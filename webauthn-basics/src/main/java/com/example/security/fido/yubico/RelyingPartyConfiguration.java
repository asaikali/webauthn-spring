package com.example.security.fido.yubico;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RelyingPartyConfiguration {
  @Bean
  RelyingParty relyingParty(CredentialRepository credentialRepository) {
    RelyingPartyIdentity rpIdentity =
        RelyingPartyIdentity.builder()
            .id("localhost") // Set this to a parent domain that covers all subdomains// where
            // users' credentials should be valid
            .name("Example Application")
            .build();

    var relyingParty =
        RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(credentialRepository)
            .allowOriginPort(true)
            .build();

    return relyingParty;
  }
}
