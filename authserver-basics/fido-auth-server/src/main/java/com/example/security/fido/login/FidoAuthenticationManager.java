package com.example.security.fido.login;

import com.example.json.JsonUtils;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class FidoAuthenticationManager implements AuthenticationManager {

  private final LoginService loginService;

  public FidoAuthenticationManager(LoginService loginService) {
    this.loginService = loginService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    var fidoToken = (FidoAuthenticationToken) authentication;

    try {
      AssertionResult assertionResult =
          this.loginService.finishLogin(fidoToken.getLoginFinishRequest());
      if (assertionResult.isSuccess()) {
        var result = new FidoAuthentication(fidoToken, JsonUtils.toJson(assertionResult));
        return result;
      }
      throw new BadCredentialsException("FIDO WebAuthn failed");
    } catch (AssertionFailedException e) {
      throw new BadCredentialsException("unable to login", e);
    }
  }
}
