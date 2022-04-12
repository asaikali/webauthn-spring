package com.example.security.user;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "user_accounts")
class UserAccountEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
      name = "UUID",
      strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "email")
  private String email;

  @OneToMany(mappedBy = "userId",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<UserCredentialEntity> credentials = Set.of();

  public Set<UserCredentialEntity> getCredentials() {
    return credentials;
  }

  public void setCredentials(Set<UserCredentialEntity> credentials) {
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
