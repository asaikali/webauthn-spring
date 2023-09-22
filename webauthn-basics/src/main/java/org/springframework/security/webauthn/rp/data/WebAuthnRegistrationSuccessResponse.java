package org.springframework.security.webauthn.rp.data;

import java.util.UUID;

public class WebAuthnRegistrationSuccessResponse {
    private UUID flowId;
    private boolean registrationComplete;

    public UUID getFlowId() {
        return this.flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public boolean isRegistrationComplete() {
        return this.registrationComplete;
    }

    public void setRegistrationComplete(boolean registrationComplete) {
        this.registrationComplete = registrationComplete;
    }

}