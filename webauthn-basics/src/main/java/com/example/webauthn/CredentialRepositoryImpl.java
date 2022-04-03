package com.example.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
class CredentialRepositoryImpl  implements CredentialRepository {

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    return Set.of();
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    return Optional.empty();
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    return Optional.empty();
  }

  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    return Optional.empty();
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    return Set.of();
  }
}
