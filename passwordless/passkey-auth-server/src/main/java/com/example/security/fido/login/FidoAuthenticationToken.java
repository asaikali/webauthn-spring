package com.example.security.fido.login;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class FidoAuthenticationToken extends AbstractAuthenticationToken {
  private final String username;
  private final LoginFinishRequest loginFinishRequest;

  public FidoAuthenticationToken(String username, LoginFinishRequest loginFinishRequest) {
    super(Collections.emptyList());
    this.username = username;
    this.loginFinishRequest = loginFinishRequest;
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
}
