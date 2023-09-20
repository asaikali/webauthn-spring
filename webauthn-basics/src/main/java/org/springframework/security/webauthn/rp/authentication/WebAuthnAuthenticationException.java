package org.springframework.security.webauthn.rp.authentication;

import org.springframework.security.core.AuthenticationException;

public class WebAuthnAuthenticationException extends AuthenticationException {

    public WebAuthnAuthenticationException(String msg) {
        super(msg);
    }

    public WebAuthnAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}