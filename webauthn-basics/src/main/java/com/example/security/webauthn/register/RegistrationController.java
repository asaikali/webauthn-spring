package com.example.security.webauthn.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
class RegistrationController {
  private final String START_REG_REQUEST = "start_reg_request";
  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping("/users/register/start")
  RegistrationStartResponse startRegisteration(@RequestBody RegistrationStartRequest request, HttpSession session) throws JsonProcessingException {
    var response = this.registrationService.startRegistration(request);
    session.setAttribute(START_REG_REQUEST, response);

    return response;
  }

  @PostMapping("/users/register/finish")
  String finishRegistration(@RequestBody RegistrationFinishRequest request, HttpSession session) throws JsonProcessingException {


    var options = (PublicKeyCredentialCreationOptions) session.getAttribute(START_REG_REQUEST);
    if( options == null ) {
      throw new IllegalArgumentException("Cloud not find start request corresponding to finish request");
    }

    PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential;

//    PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>.PublicKeyCredentialBuilder
//    this.webAuthnService.finishRegistration(options,credential);
    return options.toJson();
  }
}
