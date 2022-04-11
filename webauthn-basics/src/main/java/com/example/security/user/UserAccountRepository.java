package com.example.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserAccountRepository extends JpaRepository<UserAccountEntity,Integer> {
  Optional<UserAccountEntity> findByEmail(String email);
}
