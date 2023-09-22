package org.springframework.security.webauthn.rp.data;

import java.util.UUID;

import com.yubico.webauthn.AssertionRequest;

public class WebAuthnAssertionRequest {
    private UUID flowId;
    private AssertionRequest assertionRequest;

    public UUID getFlowId() {
        return this.flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public AssertionRequest getAssertionRequest() {
        return this.assertionRequest;
    }

    public void setAssertionRequest(AssertionRequest assertionRequest) {
        this.assertionRequest = assertionRequest;
    }

}