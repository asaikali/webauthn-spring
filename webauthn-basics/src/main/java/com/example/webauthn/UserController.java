package com.example.webauthn;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
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
  PublicKeyCredentialCreationOptions register(@RequestBody StartRegistrationRequest request) {
    var options = this.webAuthnService.startRegisteration(request);
    return options;
  }
}
