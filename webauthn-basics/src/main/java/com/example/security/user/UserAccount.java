package com.example.security.user;

import java.util.Set;
import java.util.UUID;

public record UserAccount (UUID id, String name, String email, Set<FidoCredential> credentials) {}
