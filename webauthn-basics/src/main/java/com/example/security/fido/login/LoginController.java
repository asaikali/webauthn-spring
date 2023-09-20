package com.example.security.fido.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/webauthn/login")
  String get() {
    return "fido-login";
  }

}
