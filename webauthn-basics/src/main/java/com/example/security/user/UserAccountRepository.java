package com.example.security.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

  Optional<UserAccountEntity> findByEmail(String email);

}
