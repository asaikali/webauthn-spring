package com.example.security.user;


public interface UserService {

  /**
   * Create a new user account with provided email address and fullname. If the
   * there is an account with an existing email address the account is returned.
   *
   * @param fullname the name of the user
   * @param email the email of the user
   *
   * @return full details of the newly created user or loaded from the database
   */
  UserAccount createOrFindUser(String fullname, String email);
}
