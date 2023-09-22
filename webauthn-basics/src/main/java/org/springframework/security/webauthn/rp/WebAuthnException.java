package org.springframework.security.webauthn.rp;

public class WebAuthnException extends RuntimeException {

    public WebAuthnException(String msg) {
        super(msg);
    }

    public WebAuthnException(String msg, Throwable cause) {
        super(msg, cause);
    }

}