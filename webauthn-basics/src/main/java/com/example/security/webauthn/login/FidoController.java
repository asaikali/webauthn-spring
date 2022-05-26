package com.example.security.webauthn.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FidoController {

    @GetMapping("/webauthn/login")
    String get() {
        return "fido-login";
    }
}
