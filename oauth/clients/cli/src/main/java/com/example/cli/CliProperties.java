package com.example.cli;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cli")
public class CliProperties {

  private String authServerBaseUrl = "http://localhost:9090";
  private String resourceServerBaseUrl = "http://localhost:8081";
  private String clientId = "device-client";
  private String clientSecret = "device-client-secret";
  private String scope = "quotes.read";

  public String getAuthServerBaseUrl() {
    return authServerBaseUrl;
  }

  public void setAuthServerBaseUrl(String authServerBaseUrl) {
    this.authServerBaseUrl = authServerBaseUrl;
  }

  public String getResourceServerBaseUrl() {
    return resourceServerBaseUrl;
  }

  public void setResourceServerBaseUrl(String resourceServerBaseUrl) {
    this.resourceServerBaseUrl = resourceServerBaseUrl;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}
