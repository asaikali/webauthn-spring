package com.example.security.webauthn.yubico;

import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import org.springframework.stereotype.Repository;

@Repository
public class CredentialRepositoryImpl  implements CredentialRepository {

  private final UserService userService;

  public CredentialRepositoryImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    List<UserAccount> users = this.userService.findAllUsersWithName(username);
    Set<PublicKeyCredentialDescriptor> result = new HashSet<>();

    users.stream().forEach( user -> {
      user.credentials().stream().forEach( cred -> {
        PublicKeyCredentialDescriptor descriptor = null;
        try {
          descriptor = PublicKeyCredentialDescriptor.builder()
                  .id(ByteArray.fromBase64Url(cred.keyId()))
                  .type(PublicKeyCredentialType.valueOf(cred.keyType()))
                  .build();
        } catch (Base64UrlException e) {
          throw  new RuntimeException(e);
        }
        result.add(descriptor);
      });
    });

    return result;
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    return Optional.empty();
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    return this.userService.findUserById(YubicoUtils.toUUID(userHandle)).map( userAccount -> userAccount.name());
  }

  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    return Optional.empty();
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    return this.userService.findCredentialById(credentialId.getBase64Url()).map( fidoCredential -> {
      try {
        var descriptor = RegisteredCredential.builder()
                .credentialId(credentialId)
                .userHandle(YubicoUtils.toByteArray(fidoCredential.userid()))
                .publicKeyCose(ByteArray.fromBase64Url(fidoCredential.publicKeyCose()))
                .build();
        return Set.of(descriptor);
      } catch (Base64UrlException e) {
        throw new RuntimeException(e);
      }
    }).orElse(Set.of());
  }
}
