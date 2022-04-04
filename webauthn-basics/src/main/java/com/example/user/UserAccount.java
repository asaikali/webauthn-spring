package com.example.user;

import java.util.UUID;

public record UserAccount (UUID id, String name, String email) {}
