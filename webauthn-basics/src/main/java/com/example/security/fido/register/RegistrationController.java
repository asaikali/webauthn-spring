package com.example.security.fido.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RegistrationController {
  private final String START_REG_REQUEST = "start_reg_request";
  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }



  @PostMapping("/webauthn/register/start")
  RegistrationStartResponse startRegisteration(
      @RequestBody RegistrationStartRequest request, HttpSession session)
      throws JsonProcessingException {
    var response = this.registrationService.startRegistration(request);
    session.setAttribute(START_REG_REQUEST, response);

    return response;
  }

  @PostMapping("/webauthn/register/finish")
  RegistrationFinishResponse finishRegistration(
      @RequestBody RegistrationFinishRequest request, HttpSession session)
      throws RegistrationFailedException, JsonProcessingException {
    RegistrationStartResponse response =
        (RegistrationStartResponse) session.getAttribute(START_REG_REQUEST);
    if (response == null) {
      throw new RuntimeException("Cloud Not find the original request");
    }
    var result =
        this.registrationService.finishRegistration(
            request, response.getCredentialCreationOptions());
    return result;
  }
}
