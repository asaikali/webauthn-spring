package com.example;

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

  @GetMapping("/signup")
  String signup() {
    return "signup";
  }

  @GetMapping("/login")
  String login() {
    return "login";
  }
}
