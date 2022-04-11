package com.example.security.user;

import java.util.UUID;

public record FidoCredential(String keyId, String keyType, UUID userid, String publicKeyCose) { }
