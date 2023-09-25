package org.springframework.security.webauthn.rp;

import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationResponse;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationSuccessResponse;

public interface WebAuthnRegistrationService {

    WebAuthnRegistrationRequest startRegistration(
            WebAuthnRegistrationCreateRequest registrationCreateRequest);

    WebAuthnRegistrationSuccessResponse finishRegistration(
            WebAuthnRegistrationRequest registrationRequest,
            WebAuthnRegistrationResponse registrationResponse);

}