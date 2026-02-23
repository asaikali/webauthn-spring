package com.example.tokenexchange.webapp.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping("/")
  public Map<String, Object> home() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("module", "oauth/token-exchange/tx-webapp");
    response.put("description", "Login with OAuth2 then trigger token exchange chain");
    response.put("next",
        Map.of(
            "impersonation", "GET /demo/impersonation",
            "delegation", "GET /demo/delegation"));
    return response;
  }
}
