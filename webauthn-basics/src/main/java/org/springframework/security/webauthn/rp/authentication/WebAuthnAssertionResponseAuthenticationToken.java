package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.yubico.webauthn.AssertionResult;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;

public class WebAuthnAssertionResponseAuthenticationToken extends AbstractAuthenticationToken {
    private final String userName;
    private final WebAuthnAssertionRequest assertionRequest;
    private final WebAuthnAssertionResponse assertionResponse;
    private final AssertionResult assertionResult;

    public WebAuthnAssertionResponseAuthenticationToken(String userName,
                                                        WebAuthnAssertionRequest assertionRequest,
                                                        WebAuthnAssertionResponse assertionResponse) {
        super(Collections.emptyList());
        this.userName = userName;
        this.assertionRequest = assertionRequest;
        this.assertionResponse = assertionResponse;
        this.assertionResult = null;
    }

    public WebAuthnAssertionResponseAuthenticationToken(AssertionResult assertionResult) {
        super(Collections.emptyList());
        this.assertionResult = assertionResult;
        this.userName = null;
        this.assertionRequest = null;
        this.assertionResponse = null;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        if (this.assertionResult != null) {
            return this.assertionResult.getUsername();
        } else {
            return this.assertionRequest.getAssertionRequest().getUsername().get();
        }
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    public String getUserName() {
        return this.userName;
    }

    public WebAuthnAssertionRequest getAssertionRequest() {
        return this.assertionRequest;
    }

    public WebAuthnAssertionResponse getAssertionResponse() {
        return this.assertionResponse;
    }

    public AssertionResult getAssertionResult() {
        return this.assertionResult;
    }

}