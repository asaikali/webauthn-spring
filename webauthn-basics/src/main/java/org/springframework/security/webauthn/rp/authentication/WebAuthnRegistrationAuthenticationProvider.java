package org.springframework.security.webauthn.rp.authentication;

import com.example.security.fido.register.RegistrationFinishResponse;
import com.example.security.fido.register.RegistrationService;
import com.example.security.fido.register.RegistrationStartResponse;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public final class WebAuthnRegistrationAuthenticationProvider implements AuthenticationProvider {
    private final RegistrationService registrationService;

    public WebAuthnRegistrationAuthenticationProvider(RegistrationService registrationService) {
        Assert.notNull(registrationService, "registrationService cannot be null");
        this.registrationService = registrationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return (authentication instanceof WebAuthnCredentialCreationAuthenticationToken) ?
                authenticateStartRegistration(authentication) :
                authenticateFinishRegistration(authentication);
    }

    private Authentication authenticateStartRegistration(Authentication authentication) throws AuthenticationException {
        WebAuthnCredentialCreationAuthenticationToken credentialCreationAuthentication =
                (WebAuthnCredentialCreationAuthenticationToken) authentication;

        RegistrationStartResponse registrationStartResponse;
        try {
            registrationStartResponse = this.registrationService.startRegistration(credentialCreationAuthentication.getRegistrationStartRequest());
        } catch (Exception ex) {
            throw new WebAuthnAuthenticationException("Failed to start WebAuthn registration.", ex);
        }

        return new WebAuthnCredentialCreationAuthenticationToken(registrationStartResponse);
    }

    private Authentication authenticateFinishRegistration(Authentication authentication) throws AuthenticationException {
        WebAuthnAuthenticatorAttestationAuthenticationToken authenticatorAttestationAuthentication =
                (WebAuthnAuthenticatorAttestationAuthenticationToken) authentication;

        RegistrationFinishResponse registrationFinishResponse;
        try {
            registrationFinishResponse = this.registrationService.finishRegistration(
                    authenticatorAttestationAuthentication.getRegistrationFinishRequest(),
                    authenticatorAttestationAuthentication.getCredentialCreationOptions());
        } catch (Exception ex) {
            throw new WebAuthnAuthenticationException("Failed to finish WebAuthn registration.", ex);
        }

        return new WebAuthnAuthenticatorAttestationAuthenticationToken(registrationFinishResponse);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnCredentialCreationAuthenticationToken.class.isAssignableFrom(authentication) ||
                WebAuthnAuthenticatorAttestationAuthenticationToken.class.isAssignableFrom(authentication);
    }

}