# OAuth Protocol Walkthroughs

This directory contains comment-heavy `.http` files that walk through OAuth and OIDC flows manually.

The goal is educational: instead of hiding the protocol behind framework abstractions, these walkthroughs let you execute each step as a human and inspect the HTTP requests and responses along the way.

## What These Files Are For

Each file is a guided protocol lab:

* It explains the goal of the flow at the top of the file.
* It lists prerequisites such as which sample apps to run first.
* It calls out which registered clients the walkthrough depends on.
* It uses comments between requests to explain what each step is doing and what to look for in the response.

These files are intentionally coupled to the sample authorization server configuration because the registered clients and enabled grant types live there. Even so, the walkthroughs are stored in one shared location because most of them exercise end-to-end behavior across multiple apps, not just a single module.

## Current Walkthroughs

* `resource-metadata-discovery.http` - inspect a protected resource challenge and follow discovery metadata
* `client-credentials-whoami.http` - obtain a machine token and call a protected endpoint
* `device-flow-whoami.http` - complete device authorization flow and call a protected endpoint
* `token-exchange-impersonation.http` - exchange a user-bound token and compare claims
* `token-exchange-delegation.http` - exchange with subject and actor tokens to demonstrate delegation

## How To Use Them

These files are written for an HTTP client that supports `.http` request files and embedded variables, such as the IntelliJ HTTP Client.

Run the requests from top to bottom unless the file says otherwise. Some walkthroughs require a manual browser step in the middle, for example logging in during device flow.

## Where The Assumptions Live

If a walkthrough depends on a client id, secret, redirect URI, or grant type, those assumptions come from the authorization server sample:

* `/Users/adib/dev/asaikali/webauthn-spring/oauth/auth-server`

The resource-server-facing walkthroughs also depend on:

* `/Users/adib/dev/asaikali/webauthn-spring/oauth/quotes-protected-resource`

## Why This Directory Exists

These files used to sit next to one sample app, which made them harder to find and made their purpose less obvious.

`protocol-walkthroughs` is intended to be the single place for manual, step-by-step protocol exploration in the OAuth portion of this teaching repo.
