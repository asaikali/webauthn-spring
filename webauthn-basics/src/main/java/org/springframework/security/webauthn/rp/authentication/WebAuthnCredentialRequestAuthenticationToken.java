package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.example.security.fido.login.LoginStartRequest;
import com.example.security.fido.login.LoginStartResponse;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class WebAuthnCredentialRequestAuthenticationToken extends AbstractAuthenticationToken {
    private final LoginStartRequest loginStartRequest;
    private final LoginStartResponse loginStartResponse;

    public WebAuthnCredentialRequestAuthenticationToken(LoginStartRequest loginStartRequest) {
        super(Collections.emptyList());
        this.loginStartRequest = loginStartRequest;
        this.loginStartResponse = null;
    }

    public WebAuthnCredentialRequestAuthenticationToken(LoginStartResponse loginStartResponse) {
        super(Collections.emptyList());
        this.loginStartResponse = loginStartResponse;
        this.loginStartRequest = null;
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

    public LoginStartRequest getLoginStartRequest() {
        return this.loginStartRequest;
    }

    public LoginStartResponse getLoginStartResponse() {
        return this.loginStartResponse;
    }

}