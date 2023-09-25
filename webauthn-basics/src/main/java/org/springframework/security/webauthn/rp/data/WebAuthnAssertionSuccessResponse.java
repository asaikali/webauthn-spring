package org.springframework.security.webauthn.rp.data;

import com.yubico.webauthn.AssertionResult;

public class WebAuthnAssertionSuccessResponse {
    private AssertionResult assertionResult;

    public AssertionResult getAssertionResult() {
        return this.assertionResult;
    }

    public void setAssertionResult(AssertionResult assertionResult) {
        this.assertionResult = assertionResult;
    }

}
