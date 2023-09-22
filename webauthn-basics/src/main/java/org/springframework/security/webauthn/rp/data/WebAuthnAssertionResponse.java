package org.springframework.security.webauthn.rp.data;

import java.util.UUID;

import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

public class WebAuthnAssertionResponse {
    private UUID flowId;
    private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential;

    public UUID getFlowId() {
        return this.flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> getCredential() {
        return this.credential;
    }

    public void setCredential(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential) {
        this.credential = credential;
    }

}