package com.example.security.fido.register;

import com.example.json.JsonUtils;
import com.example.security.user.FidoCredential;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.example.security.fido.yubico.YubicoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class RegistrationService {

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
   * @param startRequest the json request sent from the browser
   * @return a json object with configuration details for the javascript in the browser to use to call the webAuthn api
   *
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public RegistrationStartResponse startRegistration(RegistrationStartRequest startRequest)
      throws JsonProcessingException {

    UserAccount user =
        this.userService.createOrFindUser(startRequest.getFullName(), startRequest.getEmail());
    PublicKeyCredentialCreationOptions options = createPublicKeyCredentialCreationOptions(user);
    RegistrationStartResponse startResponse = createRegistrationStartResponse(options);
    logWorkflow(startRequest, startResponse);

    return startResponse;
  }

  private void logWorkflow(
      RegistrationStartRequest startRequest, RegistrationStartResponse startResponse)
      throws JsonProcessingException {
    var registrationEntity = new RegistrationFlowEntity();
    registrationEntity.setId(startResponse.getFlowId());
    registrationEntity.setStartRequest(JsonUtils.toJson(startRequest));
    registrationEntity.setStartResponse(JsonUtils.toJson(startResponse));
    registrationEntity.setRegistrationResult(startResponse.getCredentialCreationOptions().toJson());
    this.registrationFlowRepository.save(registrationEntity);
  }

  private RegistrationStartResponse createRegistrationStartResponse(
      PublicKeyCredentialCreationOptions options) {
    RegistrationStartResponse startResponse = new RegistrationStartResponse();
    startResponse.setFlowId(UUID.randomUUID());
    startResponse.setCredentialCreationOptions(options);
    return startResponse;
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
   * @param finishRequest the json request sen from the browser contains the public key of the user
   * @param credentialCreationOptions the options generated from the call to startRegistration() and should have been
   *                                   pulled out of the http session by the controller that calls this method
   * @return JSON object indicating success or failure of the registration
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public RegistrationFinishResponse finishRegistration(
      RegistrationFinishRequest finishRequest,
      PublicKeyCredentialCreationOptions credentialCreationOptions)
      throws RegistrationFailedException, JsonProcessingException {

    FinishRegistrationOptions options =
        FinishRegistrationOptions.builder()
            .request(credentialCreationOptions)
            .response(finishRequest.getCredential())
            .build();
    RegistrationResult registrationResult = this.relyingParty.finishRegistration(options);

    var fidoCredential =
        new FidoCredential(
            registrationResult.getKeyId().getId().getBase64Url(),
            registrationResult.getKeyId().getType().name(),
            YubicoUtils.toUUID(credentialCreationOptions.getUser().getId()),
            registrationResult.getPublicKeyCose().getBase64Url());

    this.userService.addCredential(fidoCredential);

    RegistrationFinishResponse registrationFinishResponse = new RegistrationFinishResponse();
    registrationFinishResponse.setFlowId(finishRequest.getFlowId());
    registrationFinishResponse.setRegistrationComplete(true);

    logFinishStep(finishRequest, registrationResult, registrationFinishResponse);
    return registrationFinishResponse;
  }

  private void logFinishStep(
      RegistrationFinishRequest finishRequest,
      RegistrationResult registrationResult,
      RegistrationFinishResponse registrationFinishResponse) {
    RegistrationFlowEntity registrationFlow =
        this.registrationFlowRepository
            .findById(finishRequest.getFlowId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Cloud not find a registration flow with id: "
                            + finishRequest.getFlowId()));
    registrationFlow.setFinishRequest(JsonUtils.toJson(finishRequest));
    registrationFlow.setFinishResponse(JsonUtils.toJson(registrationFinishResponse));
    registrationFlow.setRegistrationResult(JsonUtils.toJson(registrationResult));
  }
}
