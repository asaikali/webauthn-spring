# token-exchange module

This module contains three small apps that demonstrate OAuth2 Token Exchange in a service chain:

1. `tx-webapp` (port 8180)
  User logs in and calls `tx-uppercase` with the user access token.
2. `tx-uppercase` (port 8182)
  Receives the user token, exchanges it at the auth server, then calls `tx-protected-resource`.
3. `tx-protected-resource` (port 8181)
  Accepts bearer token, logs it, and returns decoded JWT claims.

## Run order

1. Start auth server:

```bash
./mvnw -pl oauth/auth-server spring-boot:run
```

2. Start protected resource:

```bash
./mvnw -pl oauth/token-exchange/tx-protected-resource spring-boot:run
```

3. Start uppercase app:

```bash
./mvnw -pl oauth/token-exchange/tx-uppercase spring-boot:run
```

4. Start webapp:

```bash
./mvnw -pl oauth/token-exchange/tx-webapp spring-boot:run
```

## Demo URLs

- `http://localhost:8180/` - overview and links
- `http://localhost:8180/demo/impersonation` - subject token exchange only
- `http://localhost:8180/demo/delegation` - subject + actor token exchange

All three services log the token they receive or send so the flow is easy to follow.
