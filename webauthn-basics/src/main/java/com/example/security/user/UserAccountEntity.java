package com.example.security.user;

import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 * Database representation of the user account, this is only used inside this package.
 */
@Entity
@Table(name = "user_accounts")
class UserAccountEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "email")
  private String email;

  // see https://hibernate.atlassian.net/browse/HHH-16593
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private Set<FidoCredentialEntity> credentials = Set.of();

  public Set<FidoCredentialEntity> getCredentials() {
    return credentials;
  }

  public void setCredentials(Set<FidoCredentialEntity> credentials) {
    this.credentials = credentials;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
