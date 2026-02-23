package com.example.cli;

import java.time.Duration;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class DeviceFlowClient {

  private static final String ANSI_BOLD = "\u001B[1m";
  private static final String ANSI_RESET = "\u001B[0m";

  private final WebClient webClient;
  private final CliProperties properties;
  private final TokenStore tokenStore;
  private final ObjectMapper objectMapper;

  public DeviceFlowClient(WebClient.Builder webClientBuilder, CliProperties properties, TokenStore tokenStore,
      ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.build();
    this.properties = properties;
    this.tokenStore = tokenStore;
    this.objectMapper = objectMapper;
  }

  public StoredToken login() {
    System.out.println();
    System.out.println(bold("=== Device Login ==="));
    System.out.println(bold("Step 1 of 3: Requesting device code..."));
    DeviceAuthorizationResult authorizationResult = startDeviceAuthorization();
    DeviceAuthorizationResponse authorization = authorizationResult.response();

    String verification = authorization.getVerificationUriComplete() != null
        ? authorization.getVerificationUriComplete()
        : authorization.getVerificationUri();
    boolean hasCompleteVerificationUri = authorization.getVerificationUriComplete() != null;

    System.out.println("Device authorization response:");
    System.out.println(authorizationResult.prettyJson());
    System.out.println();
    System.out.println(bold("Step 2 of 3: Authorize in browser"));
    System.out.println("  1. Open: " + verification);
    if (!hasCompleteVerificationUri) {
      System.out.println("  2. Enter code: " + authorization.getUserCode());
    }

    Duration pollingInterval = Duration.ofSeconds(authorization.getInterval() != null ? authorization.getInterval() : 5L);
    Instant deadline = Instant.now().plusSeconds(authorization.getExpiresIn() != null ? authorization.getExpiresIn() : 600L);
    long waitedSeconds = 0;
    int pollCount = 0;

    System.out.println();
    System.out.println(bold("Step 3 of 3: Waiting for approval"));
    System.out.println("  Poll interval: " + pollingInterval.toSeconds() + "s");
    System.out.println("  Code lifetime: " + (authorization.getExpiresIn() != null ? authorization.getExpiresIn() : 600L) + "s");

    while (Instant.now().isBefore(deadline)) {
      sleep(pollingInterval);
      waitedSeconds += pollingInterval.getSeconds();
      pollCount++;
      System.out.println("  waiting... elapsed " + formatElapsed(waitedSeconds) + " (poll " + pollCount + ")");

      DeviceTokenResult tokenResult = requestDeviceToken(authorization.getDeviceCode());
      DeviceTokenResponse tokenResponse = tokenResult.response();
      if (tokenResponse != null && tokenResponse.hasAccessToken()) {
        this.tokenStore.save(tokenResponse);
        System.out.println();
        System.out.println(bold("Authorization complete."));
        System.out.println("Token endpoint success response:");
        System.out.println(tokenResult.prettyJson());
        System.out.println("  Access token received.");
        if (tokenResponse.getScope() != null) {
          System.out.println("  Scopes: " + tokenResponse.getScope());
        }
        if (tokenResponse.getExpiresIn() != null) {
          System.out.println("  Expires in: " + tokenResponse.getExpiresIn() + "s");
        }
        System.out.println("Login complete. Token is available for this shell session.");
        System.out.println();
        System.out.println(bold("Next commands: read, whoami"));
        return this.tokenStore.loadToken().orElseThrow();
      }

      String error = tokenResponse != null ? tokenResponse.getError() : "unknown_error";
      if ("authorization_pending".equals(error)) {
        continue;
      }
      if ("slow_down".equals(error)) {
        pollingInterval = pollingInterval.plusSeconds(5);
        System.out.println("  server requested slower polling; new interval is " + pollingInterval.toSeconds() + "s");
        continue;
      }
      if ("access_denied".equals(error)) {
        throw new IllegalStateException("Device authorization was denied by the user.");
      }
      if ("expired_token".equals(error)) {
        throw new IllegalStateException("Device code expired. Run login again.");
      }

      String detail = tokenResponse != null && tokenResponse.getErrorDescription() != null
          ? tokenResponse.getErrorDescription()
          : "No details provided";
      throw new IllegalStateException("Token polling failed: " + error + " (" + detail + ")");
    }

    throw new IllegalStateException("Timed out waiting for user approval. Run login again.");
  }

  private String formatElapsed(long totalSeconds) {
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private String bold(String text) {
    return ANSI_BOLD + text + ANSI_RESET;
  }

  private DeviceAuthorizationResult startDeviceAuthorization() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", this.properties.getClientId());
    form.add("client_secret", this.properties.getClientSecret());
    form.add("scope", this.properties.getScope());

    String rawJson = this.webClient.post()
        .uri(this.properties.getAuthServerBaseUrl() + "/oauth2/device_authorization")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromFormData(form))
        .retrieve()
        .bodyToMono(String.class)
        .block();

    if (rawJson == null || rawJson.isBlank()) {
      throw new IllegalStateException("Device authorization response was empty.");
    }

    DeviceAuthorizationResponse response;
    String prettyJson;
    try {
      response = this.objectMapper.readValue(rawJson, DeviceAuthorizationResponse.class);
      Object parsed = this.objectMapper.readValue(rawJson, Object.class);
      prettyJson = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse device authorization response.", ex);
    }

    if (response == null || response.getDeviceCode() == null || response.getUserCode() == null) {
      throw new IllegalStateException("Device authorization response was missing required fields.");
    }
    return new DeviceAuthorizationResult(response, prettyJson);
  }

  private DeviceTokenResult requestDeviceToken(String deviceCode) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
    form.add("device_code", deviceCode);
    form.add("client_id", this.properties.getClientId());
    form.add("client_secret", this.properties.getClientSecret());

    String rawJson = this.webClient.post()
        .uri(this.properties.getAuthServerBaseUrl() + "/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromFormData(form))
        .exchangeToMono(response -> response
            .bodyToMono(String.class)
            .defaultIfEmpty(""))
        .block();

    if (rawJson == null || rawJson.isBlank()) {
      return new DeviceTokenResult(new DeviceTokenResponse(), "{}");
    }

    try {
      DeviceTokenResponse parsed = this.objectMapper.readValue(rawJson, DeviceTokenResponse.class);
      Object jsonObject = this.objectMapper.readValue(rawJson, Object.class);
      String pretty = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
      return new DeviceTokenResult(parsed, pretty);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse token endpoint response.", ex);
    }
  }

  private void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while polling device authorization.", ex);
    }
  }

  private record DeviceAuthorizationResult(DeviceAuthorizationResponse response, String prettyJson) {
  }

  private record DeviceTokenResult(DeviceTokenResponse response, String prettyJson) {
  }
}
