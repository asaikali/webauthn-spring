package com.example.security.fido.register;

class RegistrationStartRequest {
  private String fullName;
  private String email;

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

  @Override
  public String toString() {
    return "RegistrationStartRequest[" + "fullName=" + fullName + ", " + "email=" + email + ']';
  }
}
