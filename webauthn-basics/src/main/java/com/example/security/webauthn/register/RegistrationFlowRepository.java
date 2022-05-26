package com.example.security.webauthn.register;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationFlowRepository extends JpaRepository<RegistrationFlowEntity, UUID> {}
