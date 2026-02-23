# OAuth Device Flow CLI

This module is a Spring Shell sample client for OAuth2 Device Flow.

It supports three shell commands:

- `login` - starts device flow and stores the access token in memory
- `read` - calls `GET /random-quote` on the protected resource
- `whoami` (or `whomai`) - calls `GET /whoami` on the protected resource

## Prerequisites

Start these services in separate terminals:

1. Authorization server

```bash
./mvnw -pl oauth/auth-server spring-boot:run
```

2. Protected resource server

```bash
./mvnw -pl oauth/quotes-protected-resource spring-boot:run
```

## Build and Run the CLI

From the repository root:

```bash
./mvnw -pl oauth/clients/cli -DskipTests package
```

From `oauth/clients/cli`:

```bash
./run.sh
```

This starts an interactive Spring Shell prompt.

## Demo Flow

At the `shell:>` prompt:

1. Login with device flow:

```text
login
```

2. Complete verification in browser using the URL printed by the CLI.

3. Call the protected APIs:

```text
read
whoami
```

## Important Behavior

- Token storage is in-memory only for the running shell process.
- If you exit the shell and start again, run `login` again.

## Default Endpoints and Client

Configured in `src/main/resources/application.yml`:

- Auth server: `http://localhost:9090`
- Resource server: `http://localhost:8081`
- Client id: `device-client`
- Client secret: `device-client-secret`
- Scope: `quotes.read`

## Optional: Run Tests

```bash
./mvnw -pl oauth/clients/cli test
```
