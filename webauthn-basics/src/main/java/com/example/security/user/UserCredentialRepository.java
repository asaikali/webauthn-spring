package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserCredentialRepository extends JpaRepository<UserCredentialEntity, String> {
}
