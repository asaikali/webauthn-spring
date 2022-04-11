package com.example.security.webauthn.login;

import com.yubico.webauthn.AssertionRequest;

class LoginStartRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
