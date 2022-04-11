package com.example.security.webauthn.register;

import com.example.json.JsonUtils;
import com.example.security.user.UserAccount;
import com.example.security.user.UserService;
import com.example.security.webauthn.yubico.YubicoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
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
    private final RegistrationFlowRepository registrationFlowRepository;

    RegistrationService(RelyingParty relyingParty, UserService userService, RegistrationFlowRepository registrationFlowRepository) {
        this.userService = userService;
        this.relyingParty = relyingParty;
        this.registrationFlowRepository = registrationFlowRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public RegistrationStartResponse startRegistration(RegistrationStartRequest startRequest) throws JsonProcessingException {

        UserAccount user = this.userService.createOrFindUser(startRequest.getFullName(), startRequest.getEmail());
        PublicKeyCredentialCreationOptions options = createPublicKeyCredentialCreationOptions(user);
        RegistrationStartResponse startResponse = createRegistrationStartResponse(options);
        logWorkflow(startRequest, startResponse);

        return startResponse;
    }

  

    private void logWorkflow(RegistrationStartRequest startRequest, RegistrationStartResponse startResponse) throws JsonProcessingException {
        var registrationEntity = new RegistrationFlowEntity();
        registrationEntity.setId(startResponse.getFlowId());
        registrationEntity.setStartRequest(JsonUtils.toJson(startRequest));
        registrationEntity.setStartResponse(JsonUtils.toJson(startResponse));
        registrationEntity.setRegistrationResult(startResponse.getCredentialCreationOptions().toJson());
        this.registrationFlowRepository.save(registrationEntity);
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

    @Transactional(propagation = Propagation.REQUIRED)
    public RegistrationFinishResponse finishRegistration(RegistrationFinishRequest finishRequest, PublicKeyCredentialCreationOptions credentialCreationOptions) throws RegistrationFailedException, JsonProcessingException {

        
        FinishRegistrationOptions options = FinishRegistrationOptions.builder().request(credentialCreationOptions).response(finishRequest.getCredential()).build();
        RegistrationResult registrationResult = this.relyingParty.finishRegistration(options);

        PublicKeyCredentialDescriptor keyId = registrationResult.getKeyId();


        RegistrationFinishResponse registrationFinishResponse = new RegistrationFinishResponse();
        registrationFinishResponse.setFlowId(finishRequest.getFlowId());
        registrationFinishResponse.setRegistrationComplete(true);

        logFinishStep(finishRequest, registrationResult, registrationFinishResponse);
        return registrationFinishResponse;
    }

    private void logFinishStep(RegistrationFinishRequest finishRequest, RegistrationResult registrationResult, RegistrationFinishResponse registrationFinishResponse) {
        RegistrationFlowEntity registrationFlow = this.registrationFlowRepository.findById(finishRequest.getFlowId()).orElseThrow( ()
                -> new RuntimeException("Cloud not find a registration flow with id: " + finishRequest.getFlowId()));
        registrationFlow.setFinishRequest(JsonUtils.toJson(finishRequest));
        registrationFlow.setFinishResponse(JsonUtils.toJson(registrationFinishResponse));
        registrationFlow.setRegistrationResult(JsonUtils.toJson(registrationResult));
    }

}
