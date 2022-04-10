package com.example.webauthn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
class UserController {
  private final String START_REG_REQUEST = "start_reg_request";
  private final WebAuthnService webAuthnService;

  public UserController(WebAuthnService webAuthnService) {
    this.webAuthnService = webAuthnService;
  }



  @PostMapping("/users/register/start")
  String startRegisteration(@RequestBody StartRegistrationRequest request, HttpSession session) throws JsonProcessingException {
    var options = this.webAuthnService.startRegistration(request);
    session.setAttribute(START_REG_REQUEST, options);
    return options.toJson();
  }

  @PostMapping("/users/register/finish")
  String finishRegistration(@RequestBody FinishRegistrationRequest request, HttpSession session) throws JsonProcessingException {


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
