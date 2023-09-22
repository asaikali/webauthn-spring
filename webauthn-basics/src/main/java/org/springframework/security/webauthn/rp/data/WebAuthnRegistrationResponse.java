package org.springframework.security.webauthn.rp.data;

import java.util.UUID;

import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

public class WebAuthnRegistrationResponse {
    private UUID flowId;
    private PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;

    public UUID getFlowId() {
        return this.flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> getCredential() {
        return this.credential;
    }

    public void setCredential(PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential) {
        this.credential = credential;
    }

}
