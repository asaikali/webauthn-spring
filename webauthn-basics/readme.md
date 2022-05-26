# Exploring the Web Authentication Protocol using Spring Security 

This repo contains a spring boot application that can be used to understand how the FIDO2 WebAuthn protocol 
works. On the client side only jQuery is used to keep things simple and make it possible to understand how
everything works at a low level. On the server side the Yubico java webauthn-server library is used. 

WebAuthn requires https so there is a self-signed certificate in this project. If you hare having trouble with chrome 
not accepting the certificate then you should type `thisisunsafe` and it will let you through. 
see https://stackoverflow.com/a/31900210/438319 for more options

