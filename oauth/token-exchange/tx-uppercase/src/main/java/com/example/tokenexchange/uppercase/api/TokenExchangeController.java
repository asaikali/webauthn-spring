package com.example.tokenexchange.uppercase.api;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class TokenExchangeController {
  private static final Logger log = LoggerFactory.getLogger(TokenExchangeController.class);
  private static final String TOKEN_EXCHANGE_GRANT =
      "urn:ietf:params:oauth:grant-type:token-exchange";
  private static final String ACCESS_TOKEN_TYPE =
      "urn:ietf:params:oauth:token-type:access_token";

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String authServerTokenUrl;
  private final String exchangeClientId;
  private final String exchangeClientSecret;
  private final String actorClientId;
  private final String actorClientSecret;
  private final String downstreamWhoamiUrl;
  private final String requestedScope;

  public TokenExchangeController(
      ObjectMapper objectMapper,
      @Value("${tx.auth-server-token-url}") String authServerTokenUrl,
      @Value("${tx.exchange-client-id}") String exchangeClientId,
      @Value("${tx.exchange-client-secret}") String exchangeClientSecret,
      @Value("${tx.actor-client-id}") String actorClientId,
      @Value("${tx.actor-client-secret}") String actorClientSecret,
      @Value("${tx.downstream-whoami-url}") String downstreamWhoamiUrl,
      @Value("${tx.scope}") String requestedScope) {
    this.restClient = RestClient.builder().build();
    this.objectMapper = objectMapper;
    this.authServerTokenUrl = authServerTokenUrl;
    this.exchangeClientId = exchangeClientId;
    this.exchangeClientSecret = exchangeClientSecret;
    this.actorClientId = actorClientId;
    this.actorClientSecret = actorClientSecret;
    this.downstreamWhoamiUrl = downstreamWhoamiUrl;
    this.requestedScope = requestedScope;
  }

  @GetMapping("/exchange/{mode}")
  public Map<String, Object> exchange(
      @PathVariable String mode,
      JwtAuthenticationToken authentication,
      HttpServletRequest request) {

    if (!"impersonation".equals(mode) && !"delegation".equals(mode)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be impersonation or delegation");
    }

    String incomingAccessToken = resolveRawAccessToken(authentication, request);
    if (!StringUtils.hasText(incomingAccessToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incoming bearer token is required");
    }

    logAccessToken("tx-uppercase received incoming access token", incomingAccessToken);

    String actorAccessToken = null;
    if ("delegation".equals(mode)) {
      actorAccessToken = requestActorAccessToken();
      logAccessToken("tx-uppercase obtained actor access token", actorAccessToken);
    }

    String exchangedAccessToken = exchangeToken(incomingAccessToken, actorAccessToken);
    logAccessToken("tx-uppercase obtained exchanged access token", exchangedAccessToken);
    logAccessToken("tx-uppercase calling downstream with exchanged access token", exchangedAccessToken);

    Map<String, Object> downstreamWhoami = callDownstreamWhoami(exchangedAccessToken);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("mode", mode);
    response.put("uppercase_received_access_token", incomingAccessToken);
    response.put("uppercase_received_decoded_token", decodeAccessToken(incomingAccessToken));
    if (actorAccessToken != null) {
      response.put("uppercase_actor_access_token", actorAccessToken);
      response.put("uppercase_actor_decoded_token", decodeAccessToken(actorAccessToken));
    }
    response.put("uppercase_exchanged_access_token", exchangedAccessToken);
    response.put("uppercase_exchanged_decoded_token", decodeAccessToken(exchangedAccessToken));
    response.put("downstream_whoami", downstreamWhoami);
    return response;
  }

  private String requestActorAccessToken() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    form.add("scope", this.requestedScope);

    return requestToken(actorClientId, actorClientSecret, form, "actor client credentials");
  }

  private String exchangeToken(String subjectAccessToken, String actorAccessToken) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", TOKEN_EXCHANGE_GRANT);
    form.add("subject_token", subjectAccessToken);
    form.add("subject_token_type", ACCESS_TOKEN_TYPE);
    form.add("requested_token_type", ACCESS_TOKEN_TYPE);
    form.add("scope", this.requestedScope);

    if (StringUtils.hasText(actorAccessToken)) {
      form.add("actor_token", actorAccessToken);
      form.add("actor_token_type", ACCESS_TOKEN_TYPE);
    }

    return requestToken(exchangeClientId, exchangeClientSecret, form, "token exchange");
  }

  @SuppressWarnings("unchecked")
  private String requestToken(
      String clientId,
      String clientSecret,
      MultiValueMap<String, String> form,
      String operationName) {

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(this.authServerTokenUrl)
              .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .accept(MediaType.APPLICATION_JSON)
              .body(form)
              .retrieve()
              .body(Map.class);

      if (response == null || !StringUtils.hasText((String) response.get("access_token"))) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Authorization server returned no access_token for " + operationName);
      }

      return (String) response.get("access_token");
    } catch (RestClientResponseException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Authorization server error during "
              + operationName
              + ": "
              + ex.getStatusCode()
              + " "
              + ex.getResponseBodyAsString(),
          ex);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callDownstreamWhoami(String exchangedAccessToken) {
    try {
      return restClient
          .get()
          .uri(this.downstreamWhoamiUrl)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + exchangedAccessToken)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(Map.class);
    } catch (RestClientResponseException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Downstream call failed: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(),
          ex);
    }
  }

  private String resolveRawAccessToken(
      JwtAuthenticationToken authentication, HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null) {
      if (authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
        return authorizationHeader.substring(7).trim();
      }
      return authorizationHeader;
    }
    return authentication.getToken().getTokenValue();
  }

  private Map<String, Object> decodeAccessToken(String rawAccessToken) {
    Map<String, Object> decoded = new LinkedHashMap<>();
    if (!StringUtils.hasText(rawAccessToken)) {
      decoded.put("header", null);
      decoded.put("payload", null);
      decoded.put("signature", null);
      return decoded;
    }

    String[] sections = rawAccessToken.split("\\.");
    decoded.put("header", decodeJwtSection(sections, 0));
    decoded.put("payload", decodeJwtSection(sections, 1));
    decoded.put("signature", sections.length > 2 ? sections[2] : null);
    return decoded;
  }

  private Object decodeJwtSection(String[] sections, int index) {
    if (sections.length <= index) {
      return null;
    }

    try {
      byte[] decoded = Base64.getUrlDecoder().decode(padBase64(sections[index]));
      String asText = new String(decoded, StandardCharsets.UTF_8);
      return this.objectMapper.readValue(asText, Object.class);
    } catch (Exception ex) {
      return null;
    }
  }

  private String padBase64(String value) {
    int padding = (4 - (value.length() % 4)) % 4;
    return value + "=".repeat(padding);
  }

  private void logAccessToken(String label, String rawAccessToken) {
    log.info("{}\nraw:\n{}\ndecoded:\n{}", label, rawAccessToken, prettyDecodeAccessToken(rawAccessToken));
  }

  private String prettyDecodeAccessToken(String rawAccessToken) {
    try {
      return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(decodeAccessToken(rawAccessToken));
    } catch (Exception ex) {
      return "{\"error\":\"Failed to pretty print decoded token\"}";
    }
  }
}
