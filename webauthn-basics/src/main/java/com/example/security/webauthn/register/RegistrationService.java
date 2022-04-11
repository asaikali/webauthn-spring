package com.example.security.webauthn.register;

import com.example.json.JsonUtils;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.example.security.webauthn.yubico.YubicoUtils;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class RegistrationService {

    private final UserService userService;
    private final RelyingParty relyingParty;
    private final RegistrationRepository registrationRepository;

    RegistrationService(RelyingParty relyingParty, UserService userService, RegistrationRepository registrationRepository) {
        this.userService = userService;
        this.relyingParty = relyingParty;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    RegistrationStartResponse startRegistration(RegistrationStartRequest startRequest) {

      UserAccount user = this.userService.createOrFindUser(startRequest.getFullName(), startRequest.getEmail());
      PublicKeyCredentialCreationOptions options = createPublicKeyCredentialCreationOptions(user);
      RegistrationStartResponse startResponse = createRegistrationStartResponse(options);
      logWorkflow(startRequest, startResponse);

      return startResponse;
    }

  private void logWorkflow(RegistrationStartRequest startRequest, RegistrationStartResponse startResponse) {
    var registrationEntity = new RegistrationEntity();
    registrationEntity.setId(startResponse.getFlowId());
    registrationEntity.setStartRequest(JsonUtils.toJson(startRequest));
    registrationEntity.setStartResponse(JsonUtils.toJson(startResponse));
    this.registrationRepository.save(registrationEntity);
  }

  private RegistrationStartResponse createRegistrationStartResponse(PublicKeyCredentialCreationOptions options) {
    RegistrationStartResponse startResponse = new RegistrationStartResponse();
    startResponse.setFlowId(UUID.randomUUID());
    startResponse.setCredentialCreationOptions(options);
    return startResponse;
  }

  private PublicKeyCredentialCreationOptions createPublicKeyCredentialCreationOptions(UserAccount user) {
    var userIdentity = UserIdentity.builder().
            name(user.name()).displayName(user.name()).id(YubicoUtils.toByteArray(user.id())).build();

    var authenticatorSelectionCriteria = AuthenticatorSelectionCriteria.builder()
            .userVerification(UserVerificationRequirement.DISCOURAGED).build();

    var startRegistrationOptions = StartRegistrationOptions.builder().user(userIdentity)
            .timeout(30_000).authenticatorSelection(authenticatorSelectionCriteria).build();

    PublicKeyCredentialCreationOptions options = this.relyingParty.startRegistration(startRegistrationOptions);

    return options;
  }


  void finishRegistration(PublicKeyCredentialCreationOptions request,
                            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response) throws RegistrationFailedException {
        FinishRegistrationOptions options = FinishRegistrationOptions.builder().request(request).response(response).build();
        this.relyingParty.finishRegistration(options);
    }
}
