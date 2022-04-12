package com.example.security.webauthn.login;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FidoLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        var fidoAuthenticationToken = (FidoAuthenticationToken) authentication;
        response.setContentType("application/json");
        response.setStatus(200);
        response.getWriter().println(fidoAuthenticationToken.getAssertionResultJson());
    }
}
