package org.springframework.security.webauthn.rp.authentication;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.webauthn.rp.WebAuthnAssertionService;
import org.springframework.security.webauthn.rp.WebAuthnException;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionSuccessResponse;
import org.springframework.util.Assert;

public final class WebAuthnAssertionAuthenticationProvider implements AuthenticationProvider {
    private final WebAuthnAssertionService assertionService;

    public WebAuthnAssertionAuthenticationProvider(WebAuthnAssertionService assertionService) {
        Assert.notNull(assertionService, "assertionService cannot be null");
        this.assertionService = assertionService;
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
            assertionRequest = this.assertionService.startAssertion(assertionRequestAuthentication.getAssertionCreateRequest());
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to authenticate WebAuthn assertion request.", ex);
        }

        return new WebAuthnAssertionRequestAuthenticationToken(assertionRequest);
    }

    private Authentication authenticateAssertionResponse(Authentication authentication) throws AuthenticationException {
        WebAuthnAssertionResponseAuthenticationToken assertionResponseAuthentication =
                (WebAuthnAssertionResponseAuthenticationToken) authentication;

        WebAuthnAssertionSuccessResponse assertionSuccessResponse;
        try {
            assertionSuccessResponse = this.assertionService.finishAssertion(assertionResponseAuthentication.getAssertionRequest(),
                    assertionResponseAuthentication.getAssertionResponse());
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to authenticate WebAuthn assertion response.", ex);
        }

        return new WebAuthnAssertionResponseAuthenticationToken(assertionSuccessResponse.getAssertionResult());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnAssertionRequestAuthenticationToken.class.isAssignableFrom(authentication) ||
                WebAuthnAssertionResponseAuthenticationToken.class.isAssignableFrom(authentication);
    }

}