package com.example.security.fido.register;

import java.util.UUID;

import com.example.json.JsonUtils;
import com.example.security.fido.yubico.YubicoUtils;
import com.example.security.user.FidoCredential;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.RegistrationFailedException;

import org.springframework.security.webauthn.rp.WebAuthnRegistrationService;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationResponse;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationSuccessResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class RegistrationService implements WebAuthnRegistrationService {

  private final UserService userService;
  private final RelyingParty relyingParty;
  private final RegistrationFlowRepository registrationFlowRepository;

  RegistrationService(
      RelyingParty relyingParty,
      UserService userService,
      RegistrationFlowRepository registrationFlowRepository) {
    this.userService = userService;
    this.relyingParty = relyingParty;
    this.registrationFlowRepository = registrationFlowRepository;
  }

  /**
   * Kicks off the registration process by creating a new user account adding it to the database. Then the server
   * configures how the WebAuthn api should be called, this way the server can be as strict as it wants for example
   * the server can demand info about the authenticator that will be used so that it can only accept approved
   * authenticators.
   *
   * The rest of this method needs to be saved into the http session because the finish step requires the excat
   * java object that was returned from this method as an input.
   *
   * @param registrationCreateRequest the json request sent from the browser
   * @return a json object with configuration details for the javascript in the browser to use to call the webAuthn api
   *
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public WebAuthnRegistrationRequest startRegistration(WebAuthnRegistrationCreateRequest registrationCreateRequest) {
    UserAccount user =
        this.userService.createOrFindUser(registrationCreateRequest.getFullName(), registrationCreateRequest.getEmail());
    PublicKeyCredentialCreationOptions options = createPublicKeyCredentialCreationOptions(user);
    WebAuthnRegistrationRequest registrationRequest = createRegistrationRequest(options);
    logWorkflow(registrationCreateRequest, registrationRequest);

    return registrationRequest;
  }

  private void logWorkflow(
          WebAuthnRegistrationCreateRequest registrationCreateRequest, WebAuthnRegistrationRequest registrationRequest) {
    var registrationEntity = new RegistrationFlowEntity();
    registrationEntity.setId(registrationRequest.getFlowId());
    registrationEntity.setStartRequest(JsonUtils.toJson(registrationCreateRequest));
    registrationEntity.setStartResponse(JsonUtils.toJson(registrationRequest));
    try {
      registrationEntity.setRegistrationResult(registrationRequest.getCredentialCreationOptions().toJson());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    this.registrationFlowRepository.save(registrationEntity);
  }

  private WebAuthnRegistrationRequest createRegistrationRequest(
      PublicKeyCredentialCreationOptions options) {
    WebAuthnRegistrationRequest registrationRequest = new WebAuthnRegistrationRequest();
    registrationRequest.setFlowId(UUID.randomUUID());
    registrationRequest.setCredentialCreationOptions(options);
    return registrationRequest;
  }

  private PublicKeyCredentialCreationOptions createPublicKeyCredentialCreationOptions(
      UserAccount user) {
    var userIdentity =
        UserIdentity.builder()
            .name(user.email())
            .displayName(user.displayName())
            .id(YubicoUtils.toByteArray(user.id()))
            .build();

    var authenticatorSelectionCriteria =
        AuthenticatorSelectionCriteria.builder()
            .userVerification(UserVerificationRequirement.DISCOURAGED)
            .build();

    var startRegistrationOptions =
        StartRegistrationOptions.builder()
            .user(userIdentity)
            .timeout(30_000)
            .authenticatorSelection(authenticatorSelectionCriteria)
            .build();

    PublicKeyCredentialCreationOptions options =
        this.relyingParty.startRegistration(startRegistrationOptions);

    return options;
  }

  /**
   * This method associates a FIDO2 authenticator with a user account, by saving the details of the authenticator
   * generated public key and other metadata in the database.
   *
   * @param registrationRequest the json request sen from the browser contains the public key of the user
   * @param registrationResponse the options generated from the call to startRegistration() and should have been
   *                                   pulled out of the http session by the controller that calls this method
   * @return JSON object indicating success or failure of the registration
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public WebAuthnRegistrationSuccessResponse finishRegistration(
          WebAuthnRegistrationRequest registrationRequest, WebAuthnRegistrationResponse registrationResponse) {

    FinishRegistrationOptions options =
        FinishRegistrationOptions.builder()
            .request(registrationRequest.getCredentialCreationOptions())
            .response(registrationResponse.getCredential())
            .build();
    RegistrationResult registrationResult;
    try {
      registrationResult = this.relyingParty.finishRegistration(options);
    } catch (RegistrationFailedException e) {
      throw new RuntimeException(e);
    }

    var fidoCredential =
        new FidoCredential(
            registrationResult.getKeyId().getId().getBase64Url(),
            registrationResult.getKeyId().getType().name(),
            YubicoUtils.toUUID(registrationRequest.getCredentialCreationOptions().getUser().getId()),
            registrationResult.getPublicKeyCose().getBase64Url());

    this.userService.addCredential(fidoCredential);

    WebAuthnRegistrationSuccessResponse registrationSuccessResponse = new WebAuthnRegistrationSuccessResponse();
    registrationSuccessResponse.setFlowId(registrationResponse.getFlowId());
    registrationSuccessResponse.setRegistrationComplete(true);

    logFinishStep(registrationResponse, registrationResult, registrationSuccessResponse);
    return registrationSuccessResponse;
  }

  private void logFinishStep(
      WebAuthnRegistrationResponse registrationResponse,
      RegistrationResult registrationResult,
      WebAuthnRegistrationSuccessResponse registrationSuccessResponse) {
    RegistrationFlowEntity registrationFlow =
        this.registrationFlowRepository
            .findById(registrationResponse.getFlowId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Cloud not find a registration flow with id: "
                            + registrationResponse.getFlowId()));
    registrationFlow.setFinishRequest(JsonUtils.toJson(registrationResponse));
    registrationFlow.setFinishResponse(JsonUtils.toJson(registrationSuccessResponse));
    registrationFlow.setRegistrationResult(JsonUtils.toJson(registrationResult));
  }
}
