package org.springframework.security.webauthn.rp.authentication;

import com.example.security.fido.login.LoginService;
import com.example.security.fido.login.LoginStartResponse;
import com.yubico.webauthn.AssertionResult;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public final class WebAuthnLoginAuthenticationProvider implements AuthenticationProvider {
    private final LoginService loginService;

    public WebAuthnLoginAuthenticationProvider(LoginService loginService) {
        Assert.notNull(loginService, "loginService cannot be null");
        this.loginService = loginService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return (authentication instanceof WebAuthnCredentialRequestAuthenticationToken) ?
                authenticateStartLogin(authentication) :
                authenticateFinishLogin(authentication);
    }

    private Authentication authenticateStartLogin(Authentication authentication) throws AuthenticationException {
        WebAuthnCredentialRequestAuthenticationToken credentialRequestAuthentication =
                (WebAuthnCredentialRequestAuthenticationToken) authentication;

        LoginStartResponse loginStartResponse;
        try {
            loginStartResponse = this.loginService.startLogin(credentialRequestAuthentication.getLoginStartRequest());
        } catch (Exception ex) {
            throw new WebAuthnAuthenticationException("Failed to start WebAuthn login.", ex);
        }

        return new WebAuthnCredentialRequestAuthenticationToken(loginStartResponse);
    }

    private Authentication authenticateFinishLogin(Authentication authentication) throws AuthenticationException {
        WebAuthnAuthenticatorAssertionAuthenticationToken authenticatorAssertionAuthentication =
                (WebAuthnAuthenticatorAssertionAuthenticationToken) authentication;

        AssertionResult assertionResult;
        try {
            assertionResult = this.loginService.finishLogin(authenticatorAssertionAuthentication.getLoginFinishRequest());
        } catch (Exception ex) {
            throw new WebAuthnAuthenticationException("Failed to finish WebAuthn login.", ex);
        }

        return new WebAuthnAuthenticatorAssertionAuthenticationToken(assertionResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnCredentialRequestAuthenticationToken.class.isAssignableFrom(authentication) ||
                WebAuthnAuthenticatorAssertionAuthenticationToken.class.isAssignableFrom(authentication);
    }

}