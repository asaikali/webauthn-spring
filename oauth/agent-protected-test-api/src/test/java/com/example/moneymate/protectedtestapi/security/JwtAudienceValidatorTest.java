package com.example.moneymate.protectedtestapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtAudienceValidator")
class JwtAudienceValidatorTest {

    @Test
    @DisplayName("accepts token when required audience is present")
    void acceptsRequiredAudience() {
        JwtAudienceValidator validator = new JwtAudienceValidator("protected-test-api");
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("alg", "none"),
            Map.of("aud", List.of("protected-test-api", "other"))
        );

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    @DisplayName("rejects token when required audience is missing")
    void rejectsMissingAudience() {
        JwtAudienceValidator validator = new JwtAudienceValidator("protected-test-api");
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("alg", "none"),
            Map.of("aud", List.of("other"))
        );

        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
