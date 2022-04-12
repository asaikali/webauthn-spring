package com.example.security.webauthn.login;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class LoginController {
    private final String START_REG_REQUEST = "start_login_request";
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/users/login/start")
    LoginStartResponse loginStartResponse(@RequestBody LoginStartRequest request, HttpSession session) {
        var response = this.loginService.startLogin(request);
        session.setAttribute(START_REG_REQUEST, response.getAssertionRequest());
        return response;
    }

    @PostMapping("/users/login/finish")
    AssertionResult loginStartResponse(@RequestBody LoginFinishRequest request, HttpSession session) throws AssertionFailedException {
        var assertionRequest = (AssertionRequest) session.getAttribute(START_REG_REQUEST);
        if (assertionRequest == null) {
            throw new RuntimeException("Cloud Not find the original request");
        }

        var result = this.loginService.finishLogin(request);
        if(result.isSuccess()) {
            session.setAttribute(AssertionRequest.class.getName(),result);
        }
        return result;
    }

}
