package com.example.cli;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

  private static final Duration EXPIRY_SKEW = Duration.ofSeconds(15);

  private final Clock clock;
  private StoredToken currentToken;

  public TokenStore() {
    this.clock = Clock.systemUTC();
  }

  public Optional<StoredToken> loadToken() {
    return Optional.ofNullable(this.currentToken);
  }

  public Optional<StoredToken> loadValidToken() {
    return loadToken().filter(token -> !isExpired(token));
  }

  public boolean isExpired(StoredToken token) {
    Instant expiresAt = token.getExpiresAt();
    if (expiresAt == null) {
      return false;
    }
    return expiresAt.isBefore(this.clock.instant().plus(EXPIRY_SKEW));
  }

  public void save(DeviceTokenResponse tokenResponse) {
    StoredToken token = new StoredToken();
    token.setAccessToken(tokenResponse.getAccessToken());
    token.setTokenType(tokenResponse.getTokenType());
    token.setScope(tokenResponse.getScope());
    if (tokenResponse.getExpiresIn() != null) {
      token.setExpiresAt(this.clock.instant().plusSeconds(tokenResponse.getExpiresIn()));
    }
    this.currentToken = token;
  }
}
