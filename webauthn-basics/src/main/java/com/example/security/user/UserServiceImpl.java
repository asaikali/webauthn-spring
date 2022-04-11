package com.example.security.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
class UserServiceImpl implements UserService{

  private final UserAccountRepository userAccountRepository;

  public UserServiceImpl(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public UserAccount createOrFindUser(String fullName, String email) {
    if(fullName == null || fullName.isBlank()) {
      throw new IllegalArgumentException("fullName can't be blank");
    }
    if(email == null || email.isBlank()) {
      throw new IllegalArgumentException("email can't be blank");
    }

    UserAccountEntity userAccountEntity = this.userAccountRepository.findByEmail(email).orElseGet(
        () -> {
          UserAccountEntity result = new UserAccountEntity();
          result.setEmail(email);
          result.setFullName(fullName);
          return this.userAccountRepository.save(result);
        }
    );

    return new UserAccount(userAccountEntity.getId(),
            userAccountEntity.getFullName(),
            userAccountEntity.getEmail());
  }
}
