package com.example.security.fido.register;

import java.util.UUID;

public class RegistrationFinishResponse {
  private UUID flowId;
  private boolean registrationComplete;

  public UUID getFlowId() {
    return flowId;
  }

  public void setFlowId(UUID flowId) {
    this.flowId = flowId;
  }

  public boolean isRegistrationComplete() {
    return registrationComplete;
  }

  public void setRegistrationComplete(boolean registrationComplete) {
    this.registrationComplete = registrationComplete;
  }
}
