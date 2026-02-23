package com.example.tokenexchange.webapp.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
public class TokenExchangeDemoController {
  private static final Logger log = LoggerFactory.getLogger(TokenExchangeDemoController.class);

  private final RestClient uppercaseRestClient;
  private final ObjectMapper objectMapper;

  public TokenExchangeDemoController(RestClient uppercaseRestClient, ObjectMapper objectMapper) {
    this.uppercaseRestClient = uppercaseRestClient;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/demo/{mode}")
  @SuppressWarnings("unchecked")
  public Map<String, Object> demo(
      @PathVariable String mode,
      @RegisteredOAuth2AuthorizedClient("demoAuthServer") OAuth2AuthorizedClient client) {

    if (!"impersonation".equals(mode) && !"delegation".equals(mode)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be impersonation or delegation");
    }

    if (client == null || client.getAccessToken() == null
        || !StringUtils.hasText(client.getAccessToken().getTokenValue())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user access token available");
    }

    String userAccessToken = client.getAccessToken().getTokenValue();
    logAccessToken("tx-webapp received user access token", userAccessToken);

    Map<String, Object> uppercaseResponse =
        uppercaseRestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/api/exchange/{mode}").build(mode))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAccessToken)
            .retrieve()
            .body(Map.class);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("mode", mode);
    response.put("webapp_received_user_access_token", userAccessToken);
    response.put("uppercase_service_response", uppercaseResponse);
    return response;
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
}
