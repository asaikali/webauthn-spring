package com.example.security.webauthn.login;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/users/login/start")
    LoginStartResponse loginStartResponse(@RequestBody LoginStartRequest request) {
        return this.loginService.startLogin(request);
    }


}
