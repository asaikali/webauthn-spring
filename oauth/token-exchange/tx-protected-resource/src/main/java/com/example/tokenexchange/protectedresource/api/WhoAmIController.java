package com.example.tokenexchange.protectedresource.api;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class WhoAmIController {
  private static final Logger log = LoggerFactory.getLogger(WhoAmIController.class);

  private final ObjectMapper objectMapper;

  public WhoAmIController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @GetMapping("/whoami")
  public Map<String, Object> whoami(JwtAuthenticationToken authentication, HttpServletRequest request) {
    String rawAccessToken = resolveRawAccessToken(authentication, request);
    logAccessToken("tx-protected-resource received access token", rawAccessToken);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("raw_access_token", rawAccessToken);
    response.put("decoded_access_token", decodeAccessToken(rawAccessToken));
    return response;
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
