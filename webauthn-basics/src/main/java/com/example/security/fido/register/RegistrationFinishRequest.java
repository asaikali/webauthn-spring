package com.example.security.fido.register;

import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import java.util.UUID;

public class RegistrationFinishRequest {

  private UUID flowId;
  private PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
      credential;

  public UUID getFlowId() {
    return flowId;
  }

  public void setFlowId(UUID flowId) {
    this.flowId = flowId;
  }

  public PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
      getCredential() {
    return credential;
  }

  public void setCredential(
      PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
          credential) {
    this.credential = credential;
  }
}
