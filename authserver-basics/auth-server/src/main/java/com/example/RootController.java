package com.example;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RootController {

  @GetMapping("/")
  String get() {
    return "If you see this message you are logged into the auth-server, the current time is: " + LocalDateTime.now();
  }
}
