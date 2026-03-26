package com.example.moneymate.protectedtestapi.protectedresource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProtectedController")
class ProtectedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /protected without token returns Bearer challenge with resource metadata reference")
    void protectedWithoutTokenReturns401WithBearerHeader() throws Exception {
        mockMvc.perform(get("/protected"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                containsString("resource_metadata=\"http://localhost/.well-known/oauth-protected-resource\"")))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
            .andExpect(jsonPath("$.type").value("http://localhost/problems/authentication-required"))
            .andExpect(jsonPath("$.title").value("Authentication required"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value(
                "A Bearer access token is required. Read the WWW-Authenticate header for OAuth metadata discovery, then go to API root (/) to learn this API's authentication flow."));
    }

    @Test
    @DisplayName("GET /protected with valid scope returns diagnostics")
    void protectedWithScopeReturnsDiagnostics() throws Exception {
        mockMvc.perform(get("/protected")
                .with(jwt()
                    .jwt(jwt -> jwt
                        .subject("user-123")
                        .claim("preferred_username", "demo")
                        .claim("scope", "hypermedia.access")
                        .audience(java.util.List.of("protected-test-api")))
                    .authorities(new SimpleGrantedAuthority("SCOPE_hypermedia.access"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Protected endpoint reached with a valid access token."))
            .andExpect(jsonPath("$.timestamp").isNotEmpty())
            .andExpect(jsonPath("$.user.subject").value("user-123"))
            .andExpect(jsonPath("$.user.preferredUsername").value("demo"))
            .andExpect(jsonPath("$.user.scope").value("hypermedia.access"));
    }
}
