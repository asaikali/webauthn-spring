package com.example.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.stereotype.Service;

@Service
class WebAuthnService {

  private final RelyingParty relyingParty;
  
  WebAuthnService(CredentialRepository credentialRepository)
  {

    RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
        .id("example.com")  // Set this to a parent domain that covers all subdomains// where users' credentials should be valid
        .name("Example Application")
        .build();

   this.relyingParty = RelyingParty.builder()
       .identity(rpIdentity)
       .credentialRepository(credentialRepository)
       .build();
  }

  void registerUser(RegistrationRequest request) {
//    UserIdentity userIdentity = UserIdentity.builder().name(request.getFullName());
//    StartRegistrationOptions options = StartRegistrationOptions.builder().user();
//    this.relyingParty.startRegistration();
  }
}
