package com.example.security.fido.register;

import com.example.json.JsonUtils;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "webauthn_registration_flow")
class RegistrationFlowEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "start_request")
  private String startRequest;

  @Column(name = "start_response")
  private String startResponse;

  @Column(name = "finish_request")
  private String finishRequest;

  @Column(name = "finish_response")
  private String finishResponse;

  @Column(name = "yubico_reg_result")
  private String registrationResult;

  @Column(name = "yubico_creation_options")
  private String creationOptions;

  public String getCreationOptions() {
    return creationOptions;
  }

  public void setCreationOptions(String creationOptions) {
    this.creationOptions = creationOptions;
  }

  public String getRegistrationResult() {
    return registrationResult;
  }

  public void setRegistrationResult(String registrationResult) {
    this.registrationResult = registrationResult;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getStartRequest() {
    return startRequest;
  }

  public void setStartRequest(String startRequest) {
    this.startRequest = startRequest;
  }

  public String getStartResponse() {
    return startResponse;
  }

  public void setStartResponse(String startResponse) {
    this.startResponse = startResponse;
  }

  public String getFinishRequest() {
    return finishRequest;
  }

  public void setFinishRequest(String finishRequest) {
    this.finishRequest = finishRequest;
  }

  public String getFinishResponse() {
    return finishResponse;
  }

  public void setFinishResponse(String finishResponse) {
    this.finishResponse = finishResponse;
  }

  @Override
  public String toString() {
    return JsonUtils.toJson(this);
  }
}
