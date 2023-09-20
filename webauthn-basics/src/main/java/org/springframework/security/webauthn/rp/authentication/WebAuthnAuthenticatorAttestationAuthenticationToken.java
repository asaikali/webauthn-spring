package org.springframework.security.webauthn.rp.authentication;

import java.util.Collections;

import com.example.security.fido.register.RegistrationFinishRequest;
import com.example.security.fido.register.RegistrationFinishResponse;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class WebAuthnAuthenticatorAttestationAuthenticationToken extends AbstractAuthenticationToken {
    private final RegistrationFinishRequest registrationFinishRequest;
    private final PublicKeyCredentialCreationOptions credentialCreationOptions;
    private final RegistrationFinishResponse registrationFinishResponse;

    public WebAuthnAuthenticatorAttestationAuthenticationToken(RegistrationFinishRequest registrationFinishRequest,
                                                               PublicKeyCredentialCreationOptions credentialCreationOptions) {
        super(Collections.emptyList());
        this.registrationFinishRequest = registrationFinishRequest;
        this.credentialCreationOptions = credentialCreationOptions;
        this.registrationFinishResponse = null;
    }

    public WebAuthnAuthenticatorAttestationAuthenticationToken(RegistrationFinishResponse registrationFinishResponse) {
        super(Collections.emptyList());
        this.registrationFinishResponse = registrationFinishResponse;
        this.credentialCreationOptions = null;
        this.registrationFinishRequest = null;
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

    public RegistrationFinishRequest getRegistrationFinishRequest() {
        return this.registrationFinishRequest;
    }

    public PublicKeyCredentialCreationOptions getCredentialCreationOptions() {
        return this.credentialCreationOptions;
    }

    public RegistrationFinishResponse getRegistrationFinishResponse() {
        return this.registrationFinishResponse;
    }

}