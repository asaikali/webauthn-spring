package com.example.security.webauthn.login;

import com.example.json.JsonUtils;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.example.security.webauthn.yubico.YubicoUtils;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class LoginService {

    private final RelyingParty relyingParty;
    private final LoginFlowRepository loginFlowRepository;
    private final UserService userService;

    public LoginService(RelyingParty relyingParty, LoginFlowRepository loginFlowRepository, UserService userService) {
        this.relyingParty = relyingParty;
        this.loginFlowRepository = loginFlowRepository;
        this.userService = userService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public LoginStartResponse startLogin(LoginStartRequest loginStartRequest) {

        // Find the user in the user database
        UserAccount user = this.userService.findUserEmail(loginStartRequest.getEmail()).orElseThrow(() -> new RuntimeException("Userid does not exist"));

        // make the assertion request to send to the client
        StartAssertionOptions options = StartAssertionOptions.builder()
                .timeout(60_000)
                .userHandle(YubicoUtils.toByteArray(user.id()))
                .build();
        AssertionRequest assertionRequest =  this.relyingParty.startAssertion(options);

        LoginStartResponse loginStartResponse = new LoginStartResponse();
        loginStartResponse.setFlowId(UUID.randomUUID());
        loginStartResponse.setAssertionRequest(assertionRequest);

        LoginFlowEntity loginFlowEntity = new LoginFlowEntity();
        loginFlowEntity.setId(loginStartResponse.getFlowId());
        loginFlowEntity.setStartRequest(JsonUtils.toJson(loginStartRequest));
        loginFlowEntity.setStartResponse(JsonUtils.toJson(loginStartResponse));
        this.loginFlowRepository.save(loginFlowEntity);

        return loginStartResponse;
    }



}
