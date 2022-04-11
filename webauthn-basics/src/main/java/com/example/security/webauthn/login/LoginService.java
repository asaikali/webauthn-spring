package com.example.security.webauthn.login;

import com.example.json.JsonUtils;
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

    public LoginService(RelyingParty relyingParty, LoginFlowRepository loginFlowRepository) {
        this.relyingParty = relyingParty;
        this.loginFlowRepository = loginFlowRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public LoginStartResponse startLogin(LoginStartRequest loginStartRequest) {

        StartAssertionOptions options = StartAssertionOptions.builder().timeout(60_000).username(loginStartRequest.getEmail()).build();
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
