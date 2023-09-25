package org.springframework.security.webauthn.rp;

import org.springframework.security.webauthn.rp.data.WebAuthnAssertionCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionSuccessResponse;

public interface WebAuthnAssertionService {

    WebAuthnAssertionRequest startAssertion(
            WebAuthnAssertionCreateRequest assertionCreateRequest);

    WebAuthnAssertionSuccessResponse finishAssertion(
            WebAuthnAssertionRequest assertionRequest,
            WebAuthnAssertionResponse assertionResponse);

}