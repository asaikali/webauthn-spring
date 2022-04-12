package com.example.security.webauthn.login;

import com.example.json.JsonUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name="webauthn_login_flow")
class LoginFlowEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="start_request")
    private String startRequest;

    @Column(name="start_response")
    private String startResponse;

    @Column(name="successful_login")
    private Boolean successfulLogin;

    @Column(name="assertion_request")
    private String assertionRequest;

    @Column(name="assertion_result")
    private String assertionResult;

    public String getAssertionResult() {
        return assertionResult;
    }

    public void setAssertionResult(String assertionResponse) {
        this.assertionResult = assertionResponse;
    }

    public String getAssertionRequest() {
        return assertionRequest;
    }

    public void setAssertionRequest(String assertionRequest) {
        this.assertionRequest = assertionRequest;
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


    public Boolean getSuccessfulLogin() {
        return successfulLogin;
    }

    public void setSuccessfulLogin(Boolean successfulLogin) {
        this.successfulLogin = successfulLogin;
    }

    @Override
    public String toString() {
       return JsonUtils.toJson(this);
    }
}
