package org.springframework.security.webauthn.rp.data;

public class WebAuthnAssertionCreateRequest {
    private String email;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}