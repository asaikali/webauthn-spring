package com.example.moneymate.protectedtestapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OAuth Protected Resource Metadata")
class OAuthProtectedResourceMetadataTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /.well-known/oauth-protected-resource returns RFC 9728 metadata")
    void metadataEndpointReturnsRfc9728Metadata() throws Exception {
        mockMvc.perform(get("/.well-known/oauth-protected-resource"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.resource").value("http://localhost"))
            .andExpect(jsonPath("$.authorization_servers[0]").value("http://localhost:9090"))
            .andExpect(jsonPath("$.scopes_supported[0]").value("hypermedia.access"))
            .andExpect(jsonPath("$.bearer_methods_supported[0]").value("header"))
            .andExpect(jsonPath("$.tls_client_certificate_bound_access_tokens").value(true));
    }
}
