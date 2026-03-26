package com.example.moneymate.protectedtestapi.protectedresource;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
public class ProtectedController {

    private final Clock clock;

    public ProtectedController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/protected")
    public ProtectedResponse getProtected(@AuthenticationPrincipal Jwt jwt) {
        return new ProtectedResponse(
            "Protected endpoint reached with a valid access token.",
            Instant.now(clock),
            new ProtectedResponse.UserContext(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getAudience(),
                extractScope(jwt)
            )
        );
    }

    private static String extractScope(Jwt jwt) {
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scope) {
            return scope;
        }

        if (scopeClaim instanceof Collection<?> scopeCollection) {
            return scopeCollection.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
        }

        return "";
    }
}
