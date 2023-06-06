# Exploring the Web Authentication Protocol using Spring Security 

This repo contains a spring boot application that can be used to understand how the FIDO2 WebAuthn protocol 
works. On the client side only jQuery is used to keep things simple and make it possible to understand how
everything works at a low level. On the server side the Yubico java webauthn-server library is used. 

## Prerequisites 

* Java 17
* FIDO2 compliance authenticator any of the following should work 
    * USB authenticator (Yubikey, Google Titan Key ... etc) 
    * MacBook with a fingerprint scanner
    * Windows machine with Windows Hello 
  
## Understand the WebAuthn user experience 

* go to [https://webauthn.io/](https://webauthn.io/) test that you have a working authenticator
* go to [https://webauthn.me/](https://webauthn.me/) to see an interactive animation of what is going on with the protocol 

Now that you have an idea of what the WebAuthn and FIDO2 do lets explore the demo application. 

## Run the Application 

* Import the project into your IDE 
* Run the `WebAuthnBasicsApplication` 
* go to [http://localhost:8080](http://localhost:8080) WebAuthn requires https you will be redirected to
  [https://localhost:8443](https://localhost:8433) using a self-signed certificate in this project. 
  If you hare having trouble with chrome  not accepting the certificate then you should type `thisisunsafe` 
  and it will let you through. see [stackoverflow question](https://stackoverflow.com/a/31900210/438319) for more options

## Use the application 

The web pages in this flow show the back and forth requests between the page and the server, you 
will be able to see the JSON exchanged between the server and browser. 

* from the home page select the option to register to account 
* Fill out the registration form and follow the prompts from your browser, use chrome to get the best experience but other browsers wil work too.
* Go back to the home page and try to access the quotes link you will be redirect to the login page
* follow the promotes and login without a password 

## Explore the Application Database

This demo app uses h2 to store all the registration you can explore the database by going to 
[https://localhost:8443/h2-console](https://localhost:8443/h2-console) you can connect with the 
default username sa and empty password, just hit the connect button and you will be able to 
browse all the database tables. if you put breakpoints in the application you can explore
what gets stored at different stages of the registration process.

## Explore the registration code 

All the code that handles registration has no interaction with spring security,
we define a set of registration api endpoints that must be accessible by anyone.

### Browser JavaScript 

* start by opening `src/main/resources/templates/register.html` skim the javascript code
* In your browser console put breakpoints on the javascript methods 
* go through registration flow and watch what is going on in the browser

### Backend registration code 

* Read the code in `com.example.security.fido.yubico.RelyingPartyConfiguration` 
* skim the javadoc in `com.yubico.webauthn.CredentialRepository` this is a key interface that you must implement 
* Skim the code in `com.example.security.fido.yubico.CredentialRepositoryImpl` to see how state stored in the db
* Read the sql code in `src/main/resources/db/migration` so you can understand the table structure used to store state 
* Go to the home page, then navigate to the H2 console and browser the contents of the tables
* explore the `com.example.security.fido` package notice the three subpackages
* put breakpoints on the endpoints `com.example.security.fido.register.RegistrationController` then run through a registration flow read the comments as you step through the code 
* explore the code in the `com.example.security.fido.register` package
* explore the code in the `com.example.security.user` package

## Explore the login code

The login code is integrated into spring security by implementing a 
`org.springframework.security.authentication.AuthenticationManager` and configuring an
`org.springframework.security.web.authentication.AuthenticationFilter` 

### Browser JavaScript

* start by opening `src/main/resources/templates/fido-login.html` skim the javascript code
* In your browser console put breakpoints on the javascript methods
* go through login flow and watch what is going on in the browser

# Backend login code spring security integration 

* Read the code in `FidoAuthenticationManager`
* Read the code in `FidoAuthenticationConverter`
* Read the code in `FidoAuthenticationToken`
* Read the code in `FidoAuthentication`
* Read the code in `FidoLoginSuccessHandler`
* Read the code in `com.example.security.config.WebSecurityConfig`

# Trace through backend login code 

The first part of the login flow does not interact with spring security as it used to identify what authenticator 
should be used to login a user. 

* put break points on endpoints `com.example.security.fido.login.LoginController` trace through the execution of a login




 



