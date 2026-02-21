# Exploring the Spring Authorization Server

Prerequisites:

* Java 17

There are several apps in this directory that explore various aspects of the Spring Authorization Server using Spring
Boot 3 and Spring Authorization Server 1.0.x.

## Projects

* `auth-server` this contains the primary auth server that illustrates the various features of spring
  auth server. Runs on port port 9090

* `fido-auth-sever` An auth server that can login users using FIDO and WebAuthN API. This is not complete
  and does not fully work.

* `confidential-client` a web application doing OIDC code flow with the auth server.

* `quotes-resource-server` a spring boot API that returns JSON objects with a random quotes. This is
  configured as an OIDC resource server. You must present it with a valid JWT token for it to return
  JSON object. This resource validates the JWT token using the public key of the the `auth-server`

* `quotes-resource-server-opaque` a spring boot API that returns JSON objects with random quotes. This has the
  same functionality as `quotes-resource-server` except that it validates an OIDC access tokens it gets by making a
  request to the `auth-server` token validation endpoint.

* `public-client` an angular based SPA that uses the auth server directly in the browser using an angular library. OIDC
  code flow with PKCE is used. The angular code calls the `quotes-resource-server` using
  JWT token that the Javascript obtains during login.

* `uppercase-quotes-api` this project demonstrates how an api can use client credentials OIDC flow to obtain an JWT
  token to call the `quotes-resource-server` the business logic takes the quote returned by the `quotes-resource-serevr`
  and capitalizes it.

* `uppercase-quotes-api-opaque` this project demonstrate how to use client credentials to get an oquaue token which can
  be used to call the `quotes-resource-server-opaque` server.

## Try out the auth-server

In this scenario you will run the auth server and get familiar with some of its end points

1. run the boot in the `auth-server` project
2. go to [http://127.0.0.1:9090](http://127.0.0.1:9090) to get to the login screen
3. login in using username/password of `user/user` you should a see a messaging showing sucessful login 
4. go
   to [http://127.0.0.1:9090/.well-known/openid-configuration](http://127.0.0.1:9090/.well-known/openid-configuration)
   to see the key endpoints of the server
5. go to [http://127.0.0.1:9090/h2-console](http://127.0.0.1:9090/h2-console) 
6. click the connect button to access the h2 database and see what is stored in the auth server's database

## Try out the confidential client (classic web app)

In this scenario you will run the auth server and then a classic server side rendered web app that has single sign-on
configured. the web app will redirect the user to the auth server to login upon successful login you the web app
displays
a list of rotating random quotes.

1. Run the spring boot app in the `auth-server` project

2. Run the spring boot app in the `confidental-client`

3. Go to [http://127.0.0.1:8080/](http://127.0.0.1:8080/) and you will be redirected to the auth server login screen

4. login with username `user` & password `user` to the auth server you will be redirect to client app where
   you will see a rotating set of quotes

## Try out the public client (Angular SPA)

In this scenarios an angular client makes calls to an OIDC resource server that requires a JWT token issued
by the auth server. The angular code interacts directly with the auth server to the JWT token then uses the
JWT token it gets to call the quotes API resource server.

1. Run the spring boot app in the `auth-server` project
2. Run the spring boot app in the `quotes-resource-server` project
3. Run the angular client app
    1. cd into the `public-client` directory
    2. run `npm install`
    3. run `ng serve`
4. go to [http://localhost:4200/](http://localhost:4200/) on the browser
5. click the login button you will be redirected to the login server
6. make sure to select the checkbox to enable read scope
7. go to [http://localhost:4200/quotes](http://localhost:4200/) to see the rotating quotes

## Try out service-to-service client credentials calls

In this scenario the an API uses client credentials flow to get a JWT token that it can
use to call another API.

1. Run the spring boot app in the `auth-server` project
2. Run the spring boot app in the `quotes-resource-server` project
3. Run the spring boot app in the `uppercase-quotes-api` project
4. visit [http://127.0.0.1:8082/](http://127.0.0.1:8082/) to have the uppercase API call the resource server on 8081.
   you should see a JSON with a random quote.

## Try out service-to-service calls with opaque tokens 

In this scenario the an API uses client credentials flow to get a opaque token that it can
use to call another API.

1. Run the spring boot app in the `auth-server` project
2. Run the spring boot app in the `quotes-resource-server-opaque` project 
3. Run the spring boot app in the `uppercase-quotes-api-opaque` project
4. visit [http://127.0.0.1:8084/](http://127.0.0.1:8084/) to have the uppercase API call the resource server on 8083.
   you should see a JSON with a random quote.

