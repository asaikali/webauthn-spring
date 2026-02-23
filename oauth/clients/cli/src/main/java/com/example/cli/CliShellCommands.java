package com.example.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CliShellCommands {

  private final DeviceFlowClient deviceFlowClient;
  private final ProtectedResourceClient protectedResourceClient;
  private final TokenStore tokenStore;

  public CliShellCommands(DeviceFlowClient deviceFlowClient, ProtectedResourceClient protectedResourceClient,
      TokenStore tokenStore) {
    this.deviceFlowClient = deviceFlowClient;
    this.protectedResourceClient = protectedResourceClient;
    this.tokenStore = tokenStore;
  }

  @ShellMethod(key = "login", value = "Start OAuth2 device flow login and save access token.")
  public void login() {
    this.deviceFlowClient.login();
  }

  @ShellMethod(key = "read", value = "Call protected resource /random-quote using saved token.")
  public String read() {
    StoredToken token = requireValidToken();
    return this.protectedResourceClient.readQuote(token.getAccessToken());
  }

  @ShellMethod(key = { "whoami", "whomai" }, value = "Call protected resource /whoami using saved token.")
  public String whoami() {
    StoredToken token = requireValidToken();
    return this.protectedResourceClient.whoami(token.getAccessToken());
  }

  private StoredToken requireValidToken() {
    StoredToken token = this.tokenStore.loadToken()
        .orElseThrow(() -> new IllegalStateException("No access token found in this shell session. Run: login"));
    if (this.tokenStore.isExpired(token)) {
      throw new IllegalStateException("Access token in this shell session is expired. Run: login");
    }
    return token;
  }
}
