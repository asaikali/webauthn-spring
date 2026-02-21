package com.example.ui;

import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class PagesController {

  @GetMapping("/")
  String homePage(Model model) {
    model.addAttribute("time", LocalDateTime.now().toString());
    return "index";
  }

  @GetMapping("/register")
  String signup() {
    return "register";
  }

  @GetMapping("/login")
  String login() {
    return "login";
  }

  @GetMapping("/quotes")
  String quotes() {
    return "quotes";
  }
}
