package com.example.webauthn;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

  private final WebAuthnService webAuthnService;

  public UserController(WebAuthnService webAuthnService) {
    this.webAuthnService = webAuthnService;
  }

  @PostMapping("/users/register/start")
  String register(@RequestBody StartRegistrationRequest request) throws JsonProcessingException {
    var options = this.webAuthnService.startRegisteration(request);
    return options.toJson();
  }
}
