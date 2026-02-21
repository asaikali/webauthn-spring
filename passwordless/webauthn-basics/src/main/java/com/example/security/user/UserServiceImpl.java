package com.example.security.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserServiceImpl implements UserService {

  private final UserAccountRepository userAccountRepository;

  public UserServiceImpl(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void addCredential(FidoCredential fidoCredential) {
    FidoCredentialEntity fidoCredentialEntity = new FidoCredentialEntity();
    fidoCredentialEntity.setUserId(fidoCredential.userid());
    fidoCredentialEntity.setType(fidoCredential.keyType());
    fidoCredentialEntity.setPublicKeyCose(fidoCredential.publicKeyCose());
    fidoCredentialEntity.setId(fidoCredential.keyId());

    UserAccountEntity account =
        this.userAccountRepository
            .findById(fidoCredential.userid())
            .orElseThrow(
                () -> new RuntimeException("can't add a credential to a user that does not exist"));
    account.getCredentials().add(fidoCredentialEntity);
  }

  @Override
  public Optional<FidoCredential> findCredentialById(String credentialId) {
    return Optional.empty();
  }

  @Override
  public Optional<UserAccount> findUserById(UUID userId) {
    return this.userAccountRepository.findById(userId).map(UserServiceImpl::toUserAccount);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Optional<UserAccount> findUserEmail(String email) {
    return this.userAccountRepository.findByEmail(email).map(UserServiceImpl::toUserAccount);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public UserAccount createOrFindUser(String displayName, String email) {
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("displayName can't be blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email can't be blank");
    }

    UserAccountEntity userAccountEntity =
        this.userAccountRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  UserAccountEntity result = new UserAccountEntity();
                  result.setEmail(email);
                  result.setFullName(displayName);
                  return this.userAccountRepository.save(result);
                });

    return new UserAccount(
        userAccountEntity.getId(),
        userAccountEntity.getFullName(),
        userAccountEntity.getEmail(),
        Set.of());
  }

  private static UserAccount toUserAccount(UserAccountEntity accountEntity) {

    Set<FidoCredential> credentials =
        accountEntity.getCredentials().stream()
            .map(
                c ->
                    new FidoCredential(
                        c.getId(), c.getType(), accountEntity.getId(), c.getPublicKeyCose()))
            .collect(Collectors.toSet());

    return new UserAccount(
        accountEntity.getId(), accountEntity.getFullName(), accountEntity.getEmail(), credentials);
  }
}
