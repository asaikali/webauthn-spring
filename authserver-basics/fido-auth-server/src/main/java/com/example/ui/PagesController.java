package com.example.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
class PagesController {

  @GetMapping("/")
  String homePage(Model model) {
    model.addAttribute("time", LocalDateTime.now().toString());
    return "index";
  }

  @ResponseBody
  @GetMapping("/time")
  String time() {
    return "If you see this message you are logged into the auth-server, the current time is: " + LocalDateTime.now();
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
