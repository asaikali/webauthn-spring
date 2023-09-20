package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.example.security.fido.register.RegistrationStartRequest;
import com.example.security.fido.register.RegistrationStartResponse;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class WebAuthnCredentialCreationAuthenticationToken extends AbstractAuthenticationToken {
    private final RegistrationStartRequest registrationStartRequest;
    private final RegistrationStartResponse registrationStartResponse;

    public WebAuthnCredentialCreationAuthenticationToken(RegistrationStartRequest registrationStartRequest) {
        super(Collections.emptyList());
        this.registrationStartRequest = registrationStartRequest;
        this.registrationStartResponse = null;
    }

    public WebAuthnCredentialCreationAuthenticationToken(RegistrationStartResponse registrationStartResponse) {
        super(Collections.emptyList());
        this.registrationStartResponse = registrationStartResponse;
        this.registrationStartRequest = null;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    public RegistrationStartRequest getRegistrationStartRequest() {
        return this.registrationStartRequest;
    }

    public RegistrationStartResponse getRegistrationStartResponse() {
        return this.registrationStartResponse;
    }

}