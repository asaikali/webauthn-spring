package com.example.security.webauthn.login;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class FidoRememberMeService implements RememberMeServices {

    private final LoginService loginService;

    public FidoRememberMeService(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        var assertionResult = (AssertionResult) request.getSession().getAttribute(AssertionRequest.class.getName());
        if(assertionResult != null && assertionResult.isSuccess()) {
            return new RememberMeAuthenticationToken(assertionResult.getUserHandle().getBase64Url(), assertionResult.getUsername(),
                    List.of());
        }
        return null;
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        System.out.println(("login fail"));
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        System.out.println("LoginSuccess");
    }
}
