package com.example.security.fido.login;

import com.example.json.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class FidoAuthenticationConverter implements AuthenticationConverter {
  @Override
  public Authentication convert(HttpServletRequest request) {
    String username = request.getParameter("username");
    if (username == null || username.isBlank()) {
      throw new UsernameNotFoundException("login request does not contain a username");
    }

    String finishRequest = request.getParameter("finishRequest");
    if (finishRequest == null || finishRequest.isBlank()) {
      throw new BadCredentialsException("fido credentials missing");
    }
    var loginFinishRequest = JsonUtils.fromJson(finishRequest, LoginFinishRequest.class);
    return new FidoAuthenticationToken(username, loginFinishRequest);
  }
}
