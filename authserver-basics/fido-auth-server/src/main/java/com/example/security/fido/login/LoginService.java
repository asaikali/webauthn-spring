package com.example.security.fido.login;

import com.example.json.JsonUtils;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.json.JsonUtils.toJson;

@Service
class LoginService {

  private final RelyingParty relyingParty;
  private final LoginFlowRepository loginFlowRepository;
  private final UserService userService;

  public LoginService(
      RelyingParty relyingParty, LoginFlowRepository loginFlowRepository, UserService userService) {
    this.relyingParty = relyingParty;
    this.loginFlowRepository = loginFlowRepository;
    this.userService = userService;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AssertionResult finishLogin(LoginFinishRequest loginFinishRequest)
      throws AssertionFailedException {

    var loginFlowEntity =
        this.loginFlowRepository
            .findById(loginFinishRequest.getFlowId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "flow id " + loginFinishRequest.getFlowId() + " not found"));

    var assertionRequestJson = loginFlowEntity.getAssertionRequest();
    AssertionRequest assertionRequest = null;
    try {
      assertionRequest = AssertionRequest.fromJson(assertionRequestJson);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cloud not deserialize the assertion Request");
    }

    FinishAssertionOptions options =
        FinishAssertionOptions.builder()
            .request(assertionRequest)
            .response(loginFinishRequest.getCredential())
            .build();

    AssertionResult assertionResult = this.relyingParty.finishAssertion(options);

    loginFlowEntity.setAssertionResult(JsonUtils.toJson(assertionResult));
    loginFlowEntity.setSuccessfulLogin(assertionResult.isSuccess());

    return assertionResult;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public LoginStartResponse startLogin(LoginStartRequest loginStartRequest) {

    // Find the user in the user database
    UserAccount user =
        this.userService
            .findUserEmail(loginStartRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Userid does not exist"));

    // make the assertion request to send to the client
    StartAssertionOptions options =
        StartAssertionOptions.builder()
            .timeout(60_000)
            .username(loginStartRequest.getEmail())
            //     .userHandle(YubicoUtils.toByteArray(user.id()))
            .build();
    AssertionRequest assertionRequest = this.relyingParty.startAssertion(options);

    LoginStartResponse loginStartResponse = new LoginStartResponse();
    loginStartResponse.setFlowId(UUID.randomUUID());
    loginStartResponse.setAssertionRequest(assertionRequest);

    LoginFlowEntity loginFlowEntity = new LoginFlowEntity();
    loginFlowEntity.setId(loginStartResponse.getFlowId());
    loginFlowEntity.setStartRequest(toJson(loginStartRequest));
    loginFlowEntity.setStartResponse(toJson(loginStartResponse));
    try {
      loginFlowEntity.setAssertionRequest(assertionRequest.toJson());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    this.loginFlowRepository.save(loginFlowEntity);

    return loginStartResponse;
  }
}
