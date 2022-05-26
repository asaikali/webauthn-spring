package com.example.security.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

  void addCredential(FidoCredential fidoCredential);

  Optional<FidoCredential> findCredentialById(String id);

  Optional<UserAccount> findUserById(UUID uuid);

  Optional<UserAccount> findUserEmail(String email);

  /**
   * Create a new user account with provided email address and displayname. If the there is an
   * account with an existing email address the account is returned.
   *
   * @param displayName the displayName of the user
   * @param email the email of the user
   * @return full details of the newly created user or loaded from the database
   */
  UserAccount createOrFindUser(String displayName, String email);

  List<UserAccount> findAllUsersWithName(String name);
}
