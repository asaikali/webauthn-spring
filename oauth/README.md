# OAuth Samples

This directory contains Spring Boot OAuth/OIDC samples grouped by role and flow type.

## Prerequisites

* Java 25
* Maven Wrapper from repository root (`./mvnw`)
* Node.js + npm + Angular CLI for the SPA sample

## Project Layout

* `auth-servers/basic-auth-server` primary authorization server sample
* `auth-servers/passkey-auth-server` authorization server with passkey (WebAuthn/FIDO2) login support
* `resource-servers/quotes-jwt` resource server that validates JWT access tokens
* `resource-servers/quotes-introspection` resource server that validates tokens via introspection
* `clients/authorization-code/webapp` server-rendered web app using authorization code flow
* `clients/authorization-code/spa-pkce` Angular SPA using authorization code + PKCE
* `clients/client-credentials/uppercase-jwt` client-credentials API using JWT access tokens
* `clients/client-credentials/uppercase-introspection` client-credentials API using introspection

## Quick Start Scenarios

### Authorization Server

1. Run `./mvnw -pl oauth/auth-servers/basic-auth-server spring-boot:run`
2. Open [http://127.0.0.1:9090](http://127.0.0.1:9090)
3. Login with `user/user`
4. Inspect discovery metadata at [http://127.0.0.1:9090/.well-known/openid-configuration](http://127.0.0.1:9090/.well-known/openid-configuration)

### Authorization Code: Server Web App

1. Run `basic-auth-server`
2. Run `./mvnw -pl oauth/clients/authorization-code/webapp spring-boot:run`
3. Open [http://127.0.0.1:8080](http://127.0.0.1:8080)

### Authorization Code + PKCE: SPA

1. Run `basic-auth-server`
2. Run `quotes-jwt`
3. From `oauth/clients/authorization-code/spa-pkce`, run `npm install` then `ng serve`
4. Open [http://localhost:4200](http://localhost:4200)

### Client Credentials with JWT

1. Run `basic-auth-server`
2. Run `quotes-jwt`
3. Run `uppercase-jwt`
4. Open [http://127.0.0.1:8082](http://127.0.0.1:8082)

### Client Credentials with Introspection

1. Run `basic-auth-server`
2. Run `quotes-introspection`
3. Run `uppercase-introspection`
4. Open [http://127.0.0.1:8084](http://127.0.0.1:8084)
