package com.example;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RootController {

  @GetMapping("/")
  String get() {
    return "This is the basic-auth-server where the current time is: " + LocalDateTime.now();
  }
}
