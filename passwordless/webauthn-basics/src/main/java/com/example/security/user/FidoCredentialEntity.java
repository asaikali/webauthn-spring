package com.example.security.user;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Database representation of a fido credential entity
 */
@Entity
@Table(name = "webauthn_user_credentials")
public class FidoCredentialEntity {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "type")
  private String type;

  @Column(name = "public_key_cose")
  private String publicKeyCose;

  public String getPublicKeyCose() {
    return publicKeyCose;
  }

  public void setPublicKeyCose(String publicKeyCose) {
    this.publicKeyCose = publicKeyCose;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FidoCredentialEntity that = (FidoCredentialEntity) o;
    return id.equals(that.id)
        && userId.equals(that.userId)
        && type.equals(that.type)
        && publicKeyCose.equals(that.publicKeyCose);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, type, publicKeyCose);
  }
}
