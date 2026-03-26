package com.example.moneymate.protectedtestapi.problem;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProblemTypeController {

    private static final String AUTHENTICATION_REQUIRED_PROBLEM = """
        # Authentication Required
        
        This problem type is returned when a request targets a protected endpoint without
        a valid Bearer access token.
        
        Clients MUST inspect the `WWW-Authenticate` header and follow the
        `resource_metadata` parameter to discover OAuth protected resource metadata.
        
        AI agents SHOULD use OAuth2 Device Authorization Grant and involve the user in
        browser-based verification. AI agents MUST NOT ask for username/password in chat.
        """;

    @GetMapping(value = "/problems/authentication-required", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public String authenticationRequiredProblem() {
        return AUTHENTICATION_REQUIRED_PROBLEM;
    }
}
