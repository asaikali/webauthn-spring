package org.springframework.security.webauthn.rp.data;

public class WebAuthnRegistrationCreateRequest {
    private String fullName;
    private String email;

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}