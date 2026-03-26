# PAR Client

This sample is a confidential server-rendered OAuth 2.0 client that starts the authorization code flow using Pushed Authorization Requests (PAR).

It is intentionally close to `/Users/adib/dev/asaikali/webauthn-spring/oauth/clients/webapp` so the PAR-specific changes are easy to review:

* the authorization server exposes `/oauth2/par`
* this client still uses Spring's normal `oauth2Login()` support for the callback and token exchange
* only the authorization start is customized so the client pushes the request first and then redirects the browser with `client_id` and `request_uri`

## Run

1. Start the authorization server:
   `./mvnw -pl oauth/auth-server spring-boot:run`
2. Start this sample:
   `./mvnw -pl oauth/clients/par-client spring-boot:run`
3. Open [http://127.0.0.1:8085](http://127.0.0.1:8085)

## What To Look For

When the browser starts login, the redirect should look like:

`http://localhost:9090/oauth2/authorize?client_id=par-client&request_uri=...`

That small front-channel redirect is the key difference from the normal `webapp` sample.
