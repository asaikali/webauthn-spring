package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.yubico.webauthn.AssertionResult;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;

public class WebAuthnAssertionResponseAuthenticationToken extends AbstractAuthenticationToken {
    private final String userName;
    private final WebAuthnAssertionResponse assertionResponse;
    private final AssertionResult assertionResult;

    public WebAuthnAssertionResponseAuthenticationToken(String userName, WebAuthnAssertionResponse assertionResponse) {
        super(Collections.emptyList());
        this.userName = userName;
        this.assertionResponse = assertionResponse;
        this.assertionResult = null;
    }

    public WebAuthnAssertionResponseAuthenticationToken(AssertionResult assertionResult) {
        super(Collections.emptyList());
        this.assertionResult = assertionResult;
        this.userName = null;
        this.assertionResponse = null;
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

    public String getUserName() {
        return this.userName;
    }

    public WebAuthnAssertionResponse getAssertionResponse() {
        return this.assertionResponse;
    }

    public AssertionResult getAssertionResult() {
        return this.assertionResult;
    }

}