package com.example.webauthn;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserController {

  @PostMapping("/users/register")
  RegistrationResponse register(@RequestBody RegistrationRequest  request) {
    System.out.println(request);
    return  new RegistrationResponse();
  }

  @GetMapping("/users/register")
  String get() {
    return  "hhheee";
  }
}
