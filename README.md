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
- [Angular CLI](https://angular.io/cli) (only if you want to run `oauth/public-client`)

## Build from root

```bash
./mvnw clean package
```

For module-specific instructions, see each module's `README.md`.
