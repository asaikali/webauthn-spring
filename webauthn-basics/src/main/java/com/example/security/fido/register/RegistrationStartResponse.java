package com.example.security.fido.register;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
class RegistrationStartResponse {
  private UUID flowId;
  private PublicKeyCredentialCreationOptions credentialCreationOptions;

  public UUID getFlowId() {
    return flowId;
  }

  public void setFlowId(UUID flowId) {
    this.flowId = flowId;
  }

  public PublicKeyCredentialCreationOptions getCredentialCreationOptions() {
    return credentialCreationOptions;
  }

  public void setCredentialCreationOptions(
      PublicKeyCredentialCreationOptions credentialCreationOptions) {
    this.credentialCreationOptions = credentialCreationOptions;
  }
}
