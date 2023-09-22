package org.springframework.security.webauthn.rp.authentication;

import com.example.security.fido.login.LoginService;
import com.yubico.webauthn.AssertionResult;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.webauthn.rp.WebAuthnException;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.util.Assert;

public final class WebAuthnLoginAuthenticationProvider implements AuthenticationProvider {
    private final LoginService loginService;

    public WebAuthnLoginAuthenticationProvider(LoginService loginService) {
        Assert.notNull(loginService, "loginService cannot be null");
        this.loginService = loginService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return (authentication instanceof WebAuthnAssertionRequestAuthenticationToken) ?
                authenticateAssertionRequest(authentication) :
                authenticateAssertionResponse(authentication);
    }

    private Authentication authenticateAssertionRequest(Authentication authentication) throws AuthenticationException {
        WebAuthnAssertionRequestAuthenticationToken assertionRequestAuthentication =
                (WebAuthnAssertionRequestAuthenticationToken) authentication;

        WebAuthnAssertionRequest assertionRequest;
        try {
            assertionRequest = this.loginService.startLogin(assertionRequestAuthentication.getAssertionCreateRequest());
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to authenticate WebAuthn assertion request.", ex);
        }

        return new WebAuthnAssertionRequestAuthenticationToken(assertionRequest);
    }

    private Authentication authenticateAssertionResponse(Authentication authentication) throws AuthenticationException {
        WebAuthnAssertionResponseAuthenticationToken assertionResponseAuthentication =
                (WebAuthnAssertionResponseAuthenticationToken) authentication;

        AssertionResult assertionResult;
        try {
            assertionResult = this.loginService.finishLogin(assertionResponseAuthentication.getAssertionResponse());
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to authenticate WebAuthn assertion response.", ex);
        }

        return new WebAuthnAssertionResponseAuthenticationToken(assertionResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnAssertionRequestAuthenticationToken.class.isAssignableFrom(authentication) ||
                WebAuthnAssertionResponseAuthenticationToken.class.isAssignableFrom(authentication);
    }

}