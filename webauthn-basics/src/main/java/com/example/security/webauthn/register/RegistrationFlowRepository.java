package com.example.security.webauthn.register;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistrationFlowRepository extends JpaRepository<RegistrationFlowEntity, UUID> {
}
