package com.example.moneymate.protectedtestapi.root;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RootController")
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / returns HAL-FORMS bootstrap for OAuth device flow")
    void rootIncludesDeviceFlowBootstrapTemplate() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/prs.hal-forms+json"))
            .andExpect(jsonPath("$._links.self.href").value("http://localhost/"))
            .andExpect(jsonPath("$._links.profile.href").value("/AGENTS.md"))
            .andExpect(jsonPath("$._links.profile.type").value("text/markdown"))
            .andExpect(jsonPath("$._links.protected.href").value("/protected"))
            .andExpect(jsonPath("$._links.protected.type").value("application/json"))
            .andExpect(jsonPath("$._links.oauth-protected-resource-metadata.href")
                .value("/.well-known/oauth-protected-resource"))
            .andExpect(jsonPath("$._links.authorization-server-metadata.href")
                .value("http://localhost:9090/.well-known/openid-configuration"))
            .andExpect(jsonPath("$._templates.start-device-flow.method").value("POST"))
            .andExpect(jsonPath("$._templates.start-device-flow.target")
                .value("http://localhost:9090/oauth2/device_authorization"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[0].name").value("client_id"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[0].value")
                .value("protected-test-api-hypermedia-client"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[1].name").value("client_secret"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[1].value")
                .value("protected-test-api-secret"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[2].name").value("scope"))
            .andExpect(jsonPath("$._templates.start-device-flow.properties[2].value")
                .value("hypermedia.access"));
    }

    @Test
    @DisplayName("GET /AGENTS.md returns profile instructions")
    void agentsMdIsPublicAndContainsDeviceFlowInstructions() throws Exception {
        mockMvc.perform(get("/AGENTS.md"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/markdown"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("API Contract for AI Agents")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Do not ask the user to provide username/password in chat")));
    }

    @Test
    @DisplayName("GET /problems/authentication-required returns problem type documentation")
    void authenticationRequiredProblemTypeIsPublic() throws Exception {
        mockMvc.perform(get("/problems/authentication-required"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/markdown"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication Required")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("OAuth2 Device Authorization Grant")));
    }
}
