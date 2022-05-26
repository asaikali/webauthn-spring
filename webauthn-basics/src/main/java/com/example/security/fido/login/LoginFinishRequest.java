package com.example.security.fido.login;

import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import java.util.UUID;

public class LoginFinishRequest {
  private UUID flowId;
  private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
      credential;

  public UUID getFlowId() {
    return flowId;
  }

  public void setFlowId(UUID flowId) {
    this.flowId = flowId;
  }

  public PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
      getCredential() {
    return credential;
  }

  public void setCredential(
      PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
          credential) {
    this.credential = credential;
  }
}
