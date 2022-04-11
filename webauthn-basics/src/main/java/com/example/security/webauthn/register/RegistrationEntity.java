package com.example.security.webauthn.register;

import com.example.json.JsonUtils;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="webauthn_registration_flow")
class RegistrationEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="start_request")
    private String startRequest;

    @Column(name="start_response")
    private String startResponse;

    @Column(name="finish_request")
    private String finishRequest;

    @Column(name="finish_response")
    private String finishResponse;

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
