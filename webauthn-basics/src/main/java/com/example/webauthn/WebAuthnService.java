package com.example.webauthn;

import com.example.user.UserAccount;
import com.example.user.UserService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.springframework.stereotype.Service;

@Service
class WebAuthnService {

  private final UserService userService;
  private final RelyingParty relyingParty;

  WebAuthnService(CredentialRepository credentialRepository, UserService userService) {
    this.userService = userService;
    RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
        .id("localhost")  // Set this to a parent domain that covers all subdomains// where users' credentials should be valid
        .name("Example Application")
        .build();

    this.relyingParty = RelyingParty.builder()
        .identity(rpIdentity)
        .credentialRepository(credentialRepository)
        .build();
  }

  PublicKeyCredentialCreationOptions startRegistration(StartRegistrationRequest request) {

    UserAccount user = this.userService.createOrFindUser(request.fullName(), request.email());

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
