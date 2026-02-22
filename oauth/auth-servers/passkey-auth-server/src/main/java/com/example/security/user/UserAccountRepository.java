package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

  Optional<UserAccountEntity> findByEmail(String email);

}
