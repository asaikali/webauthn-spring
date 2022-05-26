package com.example.security.fido.login;

import com.yubico.webauthn.AssertionRequest;
import java.util.UUID;

public class LoginStartResponse {
  private UUID flowId;
  private AssertionRequest assertionRequest;

  public UUID getFlowId() {
    return flowId;
  }

  public void setFlowId(UUID flowId) {
    this.flowId = flowId;
  }

  public AssertionRequest getAssertionRequest() {
    return assertionRequest;
  }

  public void setAssertionRequest(AssertionRequest assertionRequest) {
    this.assertionRequest = assertionRequest;
  }
}
