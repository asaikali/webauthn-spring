package com.example.security.fido.login;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class FidoLoginSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authentication)
      throws IOException, ServletException {
    this.onAuthenticationSuccess(request, response, authentication);
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    var fidoAuthenticationToken = (FidoAuthentication) authentication;
    response.setContentType("application/json");
    response.setStatus(200);
    response.getWriter().println(fidoAuthenticationToken.getAssertionResultJson());
  }
}
