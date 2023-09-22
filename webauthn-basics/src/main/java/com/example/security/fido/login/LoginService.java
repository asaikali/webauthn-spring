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

import org.springframework.security.webauthn.rp.data.WebAuthnAssertionCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.json.JsonUtils.toJson;

@Service
public class LoginService {

  private final RelyingParty relyingParty;
  private final LoginFlowRepository loginFlowRepository;
  private final UserService userService;

  public LoginService(
      RelyingParty relyingParty, LoginFlowRepository loginFlowRepository, UserService userService) {
    this.relyingParty = relyingParty;
    this.loginFlowRepository = loginFlowRepository;
    this.userService = userService;
  }

  /**
   * Receives the solution to the math challenge from the start method, validates that the solution is correct
   * applies the validation logic of the FIDO protocol, and then it produces a result.
   *
   * @param loginFinishRequest
   * @return
   * @throws AssertionFailedException
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public AssertionResult finishLogin(WebAuthnAssertionResponse loginFinishRequest)
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

  /**
   * This method is used to determine if a user exists and then sends back to the browser a list of
   * public keys that can be used to log in this way the browser can pick the right authenticator and
   * complete the login process. The response includes a math challenge that the authenticator needs
   * to solve using the users private key so that the server can tell that the user is who they say
   * they are.
   *
   * @param loginStartRequest info containing the user that wants to login
   * @return configuration for the browser to use to interact with the FIDO2 authenticator using WebAuthn browser API
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public WebAuthnAssertionRequest startLogin(WebAuthnAssertionCreateRequest loginStartRequest) {

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

    WebAuthnAssertionRequest loginStartResponse = new WebAuthnAssertionRequest();
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
