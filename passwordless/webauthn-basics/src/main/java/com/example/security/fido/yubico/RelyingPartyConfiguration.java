package com.example.security.fido.yubico;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class RelyingPartyConfiguration {

  /**
   * RelyingParty is the key object in the Yubico library you must configure it once with the settings
   * that identify the server, for example the domain name of the server.  Yubico library makes no
   * assumptions about what type of database is used to store user information, so it defines an
   * interface com.yubico.webauthn.CredentialRepository that is implemented in this package.
   *
   * see Yuibco docs https://developers.yubico.com/WebAuthn/
   *
   * @param credentialRepository an implementation to save webauthn details to from the databsae
   * @return
   */
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
