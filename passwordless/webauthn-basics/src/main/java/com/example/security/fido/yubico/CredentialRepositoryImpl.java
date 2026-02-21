package com.example.security.fido.yubico;

import com.example.security.user.FidoCredential;
import com.example.security.user.UserService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class CredentialRepositoryImpl implements CredentialRepository {

  private final UserService userService;

  public CredentialRepositoryImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {

    // in our implementation the usernames are email addresses

    var result =
        this.userService
            .findUserEmail(username)
            .map(
                user ->
                    user.credentials().stream()
                        .map(CredentialRepositoryImpl::toPublicKeyCredentialDescriptor)
                        .collect(Collectors.toSet()))
            .orElse(Set.of());

    return result;
  }

  @Override
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    var result =
        this.userService.findUserEmail(username).map(user -> YubicoUtils.toByteArray(user.id()));
    return result;
  }

  @Override
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    if (userHandle.isEmpty()) {
      return Optional.empty();
    }
    var result =
        this.userService
            .findUserById(YubicoUtils.toUUID(userHandle))
            .map(userAccount -> userAccount.email());
    return result;
  }

  @Override
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    // user can have muliple credentials so we are looking first for the user,
    // then for a credential that matches;

    var result =
        this.userService
            .findUserById(YubicoUtils.toUUID(userHandle))
            .map(user -> user.credentials())
            .orElse(Set.of())
            .stream()
            .filter(
                cred -> {
                  try {
                    return credentialId.equals(ByteArray.fromBase64Url(cred.keyId()));
                  } catch (Base64UrlException e) {
                    throw new RuntimeException(e);
                  }
                })
            .findFirst()
            .map(CredentialRepositoryImpl::toRegisteredCredential);

    return result;
  }

  @Override
  public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
    var result =
        this.userService
            .findCredentialById(credentialId.getBase64Url())
            .map(CredentialRepositoryImpl::toRegisteredCredential)
            .map(r -> Set.of(r))
            .orElse(Set.of());

    return result;
  }

  private static RegisteredCredential toRegisteredCredential(FidoCredential fidoCredential) {
    try {
      return RegisteredCredential.builder()
          .credentialId(ByteArray.fromBase64Url(fidoCredential.keyId()))
          .userHandle(YubicoUtils.toByteArray(fidoCredential.userid()))
          .publicKeyCose(ByteArray.fromBase64Url(fidoCredential.publicKeyCose()))
          .build();
    } catch (Base64UrlException e) {
      throw new RuntimeException(e);
    }
  }

  private static PublicKeyCredentialDescriptor toPublicKeyCredentialDescriptor(
      FidoCredential cred) {
    PublicKeyCredentialDescriptor descriptor = null;
    try {
      return PublicKeyCredentialDescriptor.builder()
          .id(ByteArray.fromBase64Url(cred.keyId()))
          .type(PublicKeyCredentialType.valueOf(cred.keyType()))
          .build();

    } catch (Base64UrlException e) {
      throw new RuntimeException(e);
    }
  }
}
