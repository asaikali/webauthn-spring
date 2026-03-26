package com.example.moneymate.protectedtestapi.protectedresource;

import java.time.Instant;
import java.util.List;

public record ProtectedResponse(
    String message,
    Instant timestamp,
    UserContext user
) {
    public record UserContext(
        String subject,
        String preferredUsername,
        List<String> audience,
        String scope
    ) {
    }
}
