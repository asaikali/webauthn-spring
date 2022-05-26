package com.example.security.fido.login;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginFlowRepository extends JpaRepository<LoginFlowEntity, UUID> {}
