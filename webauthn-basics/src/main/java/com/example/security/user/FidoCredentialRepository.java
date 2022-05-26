package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FidoCredentialRepository extends JpaRepository<FidoCredentialEntity, String> {}
