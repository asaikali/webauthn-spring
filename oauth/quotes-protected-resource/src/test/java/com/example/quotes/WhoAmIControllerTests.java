package com.example.quotes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(WhoAmIControllerTests.TestJwtDecoderConfig.class)
class WhoAmIControllerTests {

  private static final String RAW_TOKEN =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRlYnVnLWtleSJ9.eyJzdWIiOiJhbGljZSIsInNjb3BlIjpbInF1b3Rlcy5yZWFkIiwicHJvZmlsZSJdLCJlbWFpbCI6ImFsaWNlQGV4YW1wbGUuY29tIn0.signature";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void whoamiRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/whoami")).andExpect(status().isUnauthorized());
  }

  @Test
  void whoamiReturnsRawAndDecodedAccessToken() throws Exception {
    mockMvc.perform(get("/whoami")
        .header("Authorization", "Bearer " + RAW_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.raw_access_token").value(RAW_TOKEN))
        .andExpect(jsonPath("$.decoded_access_token.header.kid").value("debug-key"))
        .andExpect(jsonPath("$.decoded_access_token.header.alg").value("RS256"))
        .andExpect(jsonPath("$.decoded_access_token.payload.sub").value("alice"))
        .andExpect(jsonPath("$.decoded_access_token.payload.email").value("alice@example.com"))
        .andExpect(jsonPath("$.decoded_access_token.payload.scope[0]").value("quotes.read"))
        .andExpect(jsonPath("$.decoded_access_token.payload.scope[1]").value("profile"))
        .andExpect(jsonPath("$.decoded_access_token.signature").value("signature"));
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestJwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder() {
      return token -> {
        if (!RAW_TOKEN.equals(token)) {
          throw new BadJwtException("Unknown test token");
        }
        return Jwt.withTokenValue(token)
            .subject("alice")
            .issuer("http://localhost:9090")
            .audience(List.of("quotes-protected-resource"))
            .issuedAt(Instant.parse("2026-02-23T03:15:11Z"))
            .expiresAt(Instant.parse("2026-02-23T03:20:11Z"))
            .notBefore(Instant.parse("2026-02-23T03:15:11Z"))
            .claim("scope", List.of("quotes.read", "profile"))
            .claim("email", "alice@example.com")
            .header("kid", "debug-key")
            .header("alg", "RS256")
            .build();
      };
    }
  }
}
