package com.example.security.fido.login;

import java.util.UUID;

import com.example.json.JsonUtils;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.exception.AssertionFailedException;

import org.springframework.security.webauthn.rp.WebAuthnAssertionService;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionSuccessResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.json.JsonUtils.toJson;

@Service
class LoginService implements WebAuthnAssertionService {

  private final RelyingParty relyingParty;
  private final LoginFlowRepository loginFlowRepository;
  private final UserService userService;

  LoginService(
      RelyingParty relyingParty, LoginFlowRepository loginFlowRepository, UserService userService) {
    this.relyingParty = relyingParty;
    this.loginFlowRepository = loginFlowRepository;
    this.userService = userService;
  }

  /**
   * This method is used to determine if a user exists and then sends back to the browser a list of
   * public keys that can be used to log in this way the browser can pick the right authenticator and
   * complete the login process. The response includes a math challenge that the authenticator needs
   * to solve using the users private key so that the server can tell that the user is who they say
   * they are.
   *
   * @param assertionCreateRequest info containing the user that wants to login
   * @return configuration for the browser to use to interact with the FIDO2 authenticator using WebAuthn browser API
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public WebAuthnAssertionRequest startAssertion(WebAuthnAssertionCreateRequest assertionCreateRequest) {

    // Find the user in the user database
    UserAccount user =
        this.userService
            .findUserEmail(assertionCreateRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Userid does not exist"));

    // make the assertion request to send to the client
    StartAssertionOptions options =
        StartAssertionOptions.builder()
            .timeout(60_000)
            .username(assertionCreateRequest.getEmail())
            //     .userHandle(YubicoUtils.toByteArray(user.id()))
            .build();
    AssertionRequest assertionRequest = this.relyingParty.startAssertion(options);

    WebAuthnAssertionRequest loginStartResponse = new WebAuthnAssertionRequest();
    loginStartResponse.setFlowId(UUID.randomUUID());
    loginStartResponse.setAssertionRequest(assertionRequest);

    LoginFlowEntity loginFlowEntity = new LoginFlowEntity();
    loginFlowEntity.setId(loginStartResponse.getFlowId());
    loginFlowEntity.setStartRequest(toJson(assertionCreateRequest));
    loginFlowEntity.setStartResponse(toJson(loginStartResponse));
    try {
      loginFlowEntity.setAssertionRequest(assertionRequest.toJson());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    this.loginFlowRepository.save(loginFlowEntity);

    return loginStartResponse;
  }

  /**
   * Receives the solution to the math challenge from the start method, validates that the solution is correct
   * applies the validation logic of the FIDO protocol, and then it produces a result.
   *
   * @param assertionResponse
   * @return
   * @throws AssertionFailedException
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public WebAuthnAssertionSuccessResponse finishAssertion(WebAuthnAssertionRequest assertionRequest, WebAuthnAssertionResponse assertionResponse) {
    var loginFlowEntity =
            this.loginFlowRepository
                    .findById(assertionRequest.getFlowId())
                    .orElseThrow(
                            () ->
                                    new RuntimeException(
                                            "flow id " + assertionRequest.getFlowId() + " not found"));

    var assertionRequestJson = loginFlowEntity.getAssertionRequest();
    AssertionRequest assertionReq = null;
    try {
      assertionReq = AssertionRequest.fromJson(assertionRequestJson);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cloud not deserialize the assertion Request");
    }

    FinishAssertionOptions options =
            FinishAssertionOptions.builder()
                    .request(assertionReq)
                    .response(assertionResponse.getCredential())
                    .build();

    AssertionResult assertionResult;
    try {
      assertionResult = this.relyingParty.finishAssertion(options);
    } catch (AssertionFailedException e) {
      throw new RuntimeException(e);
    }

    loginFlowEntity.setAssertionResult(JsonUtils.toJson(assertionResult));
    loginFlowEntity.setSuccessfulLogin(assertionResult.isSuccess());

    WebAuthnAssertionSuccessResponse assertionSuccessResponse = new WebAuthnAssertionSuccessResponse();
    assertionSuccessResponse.setAssertionResult(assertionResult);

    return assertionSuccessResponse;
  }

}
