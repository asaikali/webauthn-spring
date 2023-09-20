package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.example.security.fido.login.LoginFinishRequest;
import com.yubico.webauthn.AssertionResult;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class WebAuthnAuthenticatorAssertionAuthenticationToken extends AbstractAuthenticationToken {
    private final String userName;
    private final LoginFinishRequest loginFinishRequest;
    private final AssertionResult assertionResult;

    public WebAuthnAuthenticatorAssertionAuthenticationToken(String userName, LoginFinishRequest loginFinishRequest) {
        super(Collections.emptyList());
        this.userName = userName;
        this.loginFinishRequest = loginFinishRequest;
        this.assertionResult = null;
    }

    public WebAuthnAuthenticatorAssertionAuthenticationToken(AssertionResult assertionResult) {
        super(Collections.emptyList());
        this.assertionResult = assertionResult;
        this.userName = null;
        this.loginFinishRequest = null;
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

    public LoginFinishRequest getLoginFinishRequest() {
        return this.loginFinishRequest;
    }

    public AssertionResult getAssertionResult() {
        return this.assertionResult;
    }

}