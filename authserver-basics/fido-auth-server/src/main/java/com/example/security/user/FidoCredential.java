package com.example.security.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Defines all the information about a FIDO credential associated with a specific user account.
 *
 * @param keyId the id of key as defined by the FIDO authenticator
 * @param keyType the fido key type used value set by the authenticator indicates type of public key algo used
 * @param userid  the unique ID in the user database that this credential is associated with
 * @param publicKeyCose the public key encoded in CBOR format see https://datatracker.ietf.org/doc/html/rfc8152
 */
public record FidoCredential(String keyId, String keyType, UUID userid, String publicKeyCose) {
  public FidoCredential {
    Objects.requireNonNull(keyId);
  }
}
