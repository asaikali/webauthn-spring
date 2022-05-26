package com.example.security.fido.login;

import java.util.Set;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class FidoAuthentication extends AbstractAuthenticationToken {
  private final String username;
  private final LoginFinishRequest loginFinishRequest;
  private final String assertionResultJson;

  public FidoAuthentication(
      FidoAuthenticationToken fidoAuthenticationToken, String assertionResultJson) {
    super(Set.of());
    this.username = fidoAuthenticationToken.getUsername();
    this.loginFinishRequest = fidoAuthenticationToken.getLoginFinishRequest();
    this.assertionResultJson = assertionResultJson;
    this.setAuthenticated(true);
  }

  public String getUsername() {
    return username;
  }

  public LoginFinishRequest getLoginFinishRequest() {
    return loginFinishRequest;
  }

  @Override
  public Object getCredentials() {
    return loginFinishRequest;
  }

  @Override
  public Object getPrincipal() {
    return username;
  }

  public String getAssertionResultJson() {
    return assertionResultJson;
  }
}
