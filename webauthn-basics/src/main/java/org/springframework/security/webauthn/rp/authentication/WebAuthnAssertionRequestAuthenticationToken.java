package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;

public class WebAuthnAssertionRequestAuthenticationToken extends AbstractAuthenticationToken {
    private final WebAuthnAssertionCreateRequest assertionCreateRequest;
    private final WebAuthnAssertionRequest assertionRequest;

    public WebAuthnAssertionRequestAuthenticationToken(WebAuthnAssertionCreateRequest assertionCreateRequest) {
        super(Collections.emptyList());
        this.assertionCreateRequest = assertionCreateRequest;
        this.assertionRequest = null;
    }

    public WebAuthnAssertionRequestAuthenticationToken(WebAuthnAssertionRequest assertionRequest) {
        super(Collections.emptyList());
        this.assertionRequest = assertionRequest;
        this.assertionCreateRequest = null;
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

    public WebAuthnAssertionCreateRequest getAssertionCreateRequest() {
        return this.assertionCreateRequest;
    }

    public WebAuthnAssertionRequest getAssertionRequest() {
        return this.assertionRequest;
    }

}