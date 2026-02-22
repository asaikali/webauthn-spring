# webauthn-spring

This repository contains demos for:

- Spring Authorization Server
- FIDO2 WebAuthn with Spring Boot

## Repository layout

- `passwordless/`
  - `webauthn-basics/` WebAuthn sample app
- `oauth/`
  - Multi-module Spring Authorization Server samples

## Prerequisites

- Java 21
- Maven Wrapper (`./mvnw` from repo root)
- [Angular CLI](https://angular.io/cli) (only if you want to run `oauth/clients/authorization-code/spa-pkce`)

## Build from root

```bash
./mvnw clean package
```

For module-specific instructions, see each module's `README.md`.

## Watch Out: Cookies and Oauth Redirects 

When running the demo apps locally, a successful auth-server login can still fail on the client callback if hostnames and session cookies are not aligned.

### Symptoms

- You start at `http://localhost:8080/oauth2/authorization/demoAuthServer`.
- Auth server login succeeds.
- Browser redirects back to the client, then immediately lands on `http://localhost:8080/login?error` (or `http://127.0.0.1:8080/login?error`).

### Root Cause

- The OAuth `redirect-uri` was hard-coded to `http://127.0.0.1:8080/...`.
- If the flow starts on `localhost`, callback host and original host differ, so OAuth state/session validation fails.
- Both apps used the default `JSESSIONID` cookie on `localhost`.
- Browser cookies are scoped by host/path, not port, so auth server login can overwrite the client app session cookie.
- Overwritten client session means lost OAuth authorization request state, which triggers `/login?error`.

### Fix

1. Use host-aware redirect URIs in the web client:
   - Set `spring.security.oauth2.client.registration.demoAuthServer.redirect-uri` to:
     - `{baseUrl}/login/oauth2/code/{registrationId}`

2. Use different session cookie names for client and auth server:
   - Client app (`oauth/clients/authorization-code/webapp`): `WEBAPPSESSION`
   - Auth servers (`oauth/auth-servers/*-auth-server`): `AUTHSERVERSESSION`
