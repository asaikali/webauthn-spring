package com.example.quotes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProtectedResourceMetadataTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void protectedResourceMetadataAdvertisesAuthorizationServerAndScopes() throws Exception {
    mockMvc.perform(get("/.well-known/oauth-protected-resource"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resource").value("http://localhost:8081"))
        .andExpect(jsonPath("$.authorization_servers[0]").value("http://localhost:9090"))
        .andExpect(jsonPath("$.scopes_supported[0]").value("quotes.read"))
        .andExpect(jsonPath("$.resource_name").value("quotes-protected-resource"))
        .andExpect(jsonPath("$.tls_client_certificate_bound_access_tokens").value(false));
  }
}
