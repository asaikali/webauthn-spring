# auth-server

This is a demo spring authorization server. The code in the project is heavily commented give it a read to learn 
more about the code works. At a high level the code in the project.

* Configures a typical spring security filter chain that is used to log users into the application via a test 
  in memory user details service. 

* configure a OIDC spring security filter chain, this chain is what implements the Authorization server functionality

## Test clients

The auth server seeds a few test clients at startup. For token exchange, use:

* `uppercase-quotes` with secret `uppercase-quotes` (grants: `client_credentials`, `urn:ietf:params:oauth:grant-type:token-exchange`)
* `uppercase-quotes-opaque` with secret `uppercase-quotes-opaque` (grants: `client_credentials`, `urn:ietf:params:oauth:grant-type:token-exchange`)
* `device-client` with secret `device-client-secret` (grant: `urn:ietf:params:oauth:grant-type:device_code`)
* `protected-test-api-hypermedia-client` with secret `protected-test-api-secret` (grant: `urn:ietf:params:oauth:grant-type:device_code`)
