package org.springframework.security.webauthn.rp.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebAuthnRegistrationRequest {
    private UUID flowId;
    private PublicKeyCredentialCreationOptions credentialCreationOptions;

    public UUID getFlowId() {
        return this.flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public PublicKeyCredentialCreationOptions getCredentialCreationOptions() {
        return this.credentialCreationOptions;
    }

    public void setCredentialCreationOptions(PublicKeyCredentialCreationOptions credentialCreationOptions) {
        this.credentialCreationOptions = credentialCreationOptions;
    }

}