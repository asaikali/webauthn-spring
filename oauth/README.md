# OAuth Samples

This directory contains Spring Boot OAuth/OIDC samples grouped by role and flow type.

## Prerequisites

* Java 25
* Maven Wrapper from repository root (`./mvnw`)
* Node.js + npm + Angular CLI for the SPA sample

## Project Layout

* `auth-server` primary authorization server sample
* `protocol-walkthroughs` manual, comment-guided HTTP walkthroughs for learning the protocols step by step
* `quotes-protected-resource` resource server that validates JWT access tokens
* `token-introspection/quotes-introspection` resource server that validates tokens via introspection
* `clients/webapp` server-rendered web app using authorization code flow
* `clients/par-client` server-rendered web app using authorization code flow with PAR
* `clients/spa` Angular SPA using authorization code + PKCE
* `clients/service` client-credentials API using JWT access tokens
* `token-introspection/uppercase-introspection` client-credentials API using introspection

## Protocol Walkthroughs

For manual protocol exploration, see:

* `/Users/adib/dev/asaikali/webauthn-spring/oauth/protocol-walkthroughs`

These `.http` files are written as guided walkthroughs with comments between requests so you can execute flows one step at a time and inspect the protocol directly.

## Quick Start Scenarios

### Authorization Server

1. Run `./mvnw -pl oauth/auth-server spring-boot:run`
2. Open [http://127.0.0.1:9090](http://127.0.0.1:9090)
3. Login with `user/user`
4. Inspect discovery metadata at [http://127.0.0.1:9090/.well-known/openid-configuration](http://127.0.0.1:9090/.well-known/openid-configuration)

### Authorization Code: Server Web App

1. Run `auth-server`
2. Run `./mvnw -pl oauth/clients/webapp spring-boot:run`
3. Open [http://127.0.0.1:8080](http://127.0.0.1:8080)

### Authorization Code + PAR: Server Web App

1. Run `auth-server`
2. Run `./mvnw -pl oauth/clients/par-client spring-boot:run`
3. Open [http://127.0.0.1:8085](http://127.0.0.1:8085)

### Authorization Code + PKCE: SPA

1. Run `auth-server`
2. Run `quotes-protected-resource`
3. From `oauth/clients/spa`, run `npm install` then `ng serve`
4. Open [http://localhost:4200](http://localhost:4200)

### Client Credentials with JWT

1. Run `auth-server`
2. Run `quotes-protected-resource`
3. Run `service`
4. Open [http://127.0.0.1:8082](http://127.0.0.1:8082)

### Client Credentials with Introspection

1. Run `auth-server`
2. Run `quotes-introspection`
3. Run `uppercase-introspection`
4. Open [http://127.0.0.1:8084](http://127.0.0.1:8084)
