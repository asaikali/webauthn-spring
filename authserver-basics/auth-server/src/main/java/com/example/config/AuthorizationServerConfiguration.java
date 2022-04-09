package com.example.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * <p>
 * The configuration of the authorization server is done by conifguring a bunch af
 * beans. Depending on which beans are defined different features of the autorization server
 * will be turned on or off.
 * </p>
 *
 * <p>
 * Our goal in this example is to configure enough beans to turn on OpenID connect and allow
 * confidential and public clients to authenticate using this server.
 * </p>
 *
 */
@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfiguration {

    /**
     * <p>
     * The authorization server produces JWT tokens that are digitally signed. So the
     * auth server needs public-private key pair to use sign JWT tokens that it issues.
     * This method is used to provides a list of key pairs for JWT token signing.
     * </p>
     *
     * <p>
     * <b>WARNING</b>I n this sample application we are generating a key pair everytime
     * the auth server starts. However, in a real world scenario you will need to store
     * the auth server signing keys in a secure location such as a key vault.
     * <p>
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {

        // generate a random RSA keypair using the features of the JDK
        // in production we would be getting the key pair from a key vault
        // for demo purpose this is easier to do
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        // we need a way to convert the JDK RSA key pair into a JSON formatted JSON Web Key (JWK)
        // we will use the Nimbus JOSE framework to accomplish this task.

        // A Nimbus RSA key from the JDK key pair
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();

        // Add the JWK to the key set. The standards require that AuthServers return a key set rather
        // than a single key. A key set can contain expired keys and new keys, using a key set makes
        // key rotation practical.
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * <p>
     * The authorization server has two protected endpoints /userinfo and /clientregisteration which
     * are used to get information about the current logged in user and to allow for dynamic registration
     * of application clients.
     * </p>
     *
     * <p>
     * When a client application calls the /uerinfo or /clientregisteration endpoints it provides a bearer
     * token which can be JWT or an opaque token. If a JWT is used then we need a way to decode the JWT
     * and this is what this bean defines.
     * </p>
     *
     * <p>
     * not needed if this is using opaque access tokens.
     * </p>
     *
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * <p>
     *  This bean defines the ProviderSettings which is the configuration of the Authentication
     *  server. ProviderSettings is used to configure the Urls used by the protocol end points.
     *  Most of the time we don't need to change this so we will just a default configuration.
     * </p>
     *
     * <p>
     *  The settings configured here will be visible when you visit /.well-known/openid-configuration
     *  to get a JSON object with where all the key urls are for use by an OpenIDConnect client
     * </p>
     *
     */
    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }

    /**
     * <p>
     * The authorization server only talks to applications it knows about. The auth server
     * keeps track of a known application using a RegisteredClient object. The Registered client
     * object needs to be persisted in database but the auth server does not want to dictate a
     * specific type of database to use. So the auth server defines an interface called
     * RegisteredClientRepository which is used to save and load details of apps that the
     * auth server is allowed to talk to.
     * </p>
     *
     * <p>
     * The spring authorization server ships with 2 implementations of RegisteredClientRepository:
     * InMemoryRegisteredClientRepository suitable for tests and sample apps, and
     * JdbcRegisteredClientRepository which can be used in production with a SQL database.
     * </p>
     *
     * <p>
     * In this sample we copied the sql schemas that is expected by the JdbcRegisteredClientRepository
     * from the the spring auth server .jar file into src/main/resources/db/migration and have flyway
     * creating the database tables. You can inspect what is in the database by going via the h2-console
     * see the application.yml for details.
     * </p>
     *
     * <p>
     *  In a real world scenario you likely want to provide a custom implementation of
     *  RegisteredClientRepository to meet your specific needs. You will also need to provide
     *  a way to register client applications. In this sample app we hard code two test
     *  clients to make the app simple.
     *  </p>
     *
     *  <p><b>We initialize two test clients in the AuthServerApplication class</b></p>
     *
     * @return The client repository that will be used by the auth server to determine how it should
     * interact with a client application that wants to use the auth server.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        var repo =  new JdbcRegisteredClientRepository(jdbcTemplate);
        this.createConfidentialTestClient(repo);
        this.createPublicTestClient(repo);
        return repo;
    }

    /**
     * <p>
     * The spring authorization server needs a place to track of which clients have been issued access, refresh,
     * and id tokens. When various tokens expire and various other attributes required to implement the
     * oAuth2 and OIDC protocols. Spring authorization server tracks all the attributes in java object
     * org.springframework.security.oauth2.server.authorization.OAuth2Authorization this object needs to stored
     * in persistent store.
     * </p>
     *
     * <p>
     * The Spring Authorization Server does not want to dictate what kind of database to use to store the
     * authorization details, so it expect a bean of type OAuth2AuthorizationService to be available. There are
     * two out of the box implementations of OAuth2AuthorizationService: JdbcOAuth2AuthorizationService, and
     * InMemoryOAuth2AuthorizationService. The in memory implementation is only useful for tests. The jdbc
     * implementation can be used for production.
     * <p/>
     *
     * <p>
     * In this sample we copied the sql schemas that is expected by the OAuth2AuthorizationService
     * from the the spring auth server .jar file into src/main/resources/db/migration and have flyway
     * creating the database tables. You can inspect what is in the database by going via the h2-console
     * see the application.yml for details.
     * </p>
     *
     * <p>
     * Depending on your requirements you might need to provide you own implementation of the
     * OAuth2AuthorizationService to store the results in a NoSQL database or control the table structure
     * used for a SQL database.
     * </p>
     *
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate,registeredClientRepository);
    }

    /**
     * <p>
     *  Typically a client application wants to call an api on a resource server. When the resource server
     *  gets a request it must check if the caller is allowed to perform the request application. The resource
     *  server does this by checking that the access token has the required scope. The auth server requires
     *  the consent of the user to generate an access token with a specific scopes in it. The auth server asks
     *  for user consent and records the result of what the user is consenting to in its database.
     * </p>
     *
     * <p>
     * The Spring authorization server does not want to dictate what kind of database you are going to use to store
     * the results of users granting consent for a request. Therefore, you are expected to provide an
     * implementation of the OAuth2AuthorizationConsentService interface. Two implementations of
     * OAuth2AuthorizationConsentService are provided out of the box: JdbcOAuth2AuthorizationConsentService,
     * InMemoryOAuth2AuthorizationConsentService. The in memory implementation is only useful for testing,
     * the jdbc implementation can be used in production.
     * </p>
     *
     * <p>
     * In this sample we copied the sql schemas that is expected by the OAuth2AuthorizationConsentService
     * from the the spring auth server .jar file into src/main/resources/db/migration and have flyway
     * creating the database tables. You can inspect what is in the database by going via the h2-console
     * see the application.yml for details.
     * </p>
     *
     * <p>
     * Depending on your requirements you might need to provide you own implementation of the
     * OAuth2AuthorizationConsentService to store the results in a NoSQL database or control the table structure
     * used for a SQL database.
     * </p>
     */
    @Bean
    public OAuth2AuthorizationConsentService consentService(JdbcTemplate jdbcTemplate,
                                                            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate,registeredClientRepository);
    }


    /**
     * <p>
     *  The OIDC and oAuth specs define a confidential client as a client that can securely store
     *  token issues by the auth server and can authenticate itself to the authserver. Confidential
     *  clients are typically backend server applications that are executed in a secure environment.
     * </p>
     *
     * <p>
     *  Confidential clients are given an access token and a refresh token. When the access token
     *  expires the refresh token is used to obtain the access token. You might wonder why is that
     *  we need a refresh token?
     * </p>
     *
     * <p>
     *  The access token is used by the confidential client to make API calls to resource servers.
     *  The resource servers can leak the access token via bugs or other vulnerabilities. So access
     *  tokens must have a short duration so that a hacker that gets their hands on the access
     *  token can not use them for long. The refresh token is only ever used in calls between
     *  the client application and the authorization server, so long as the client application
     *  can keep the refresh token secure stored and the authorization server is not hacked, it is
     *  safe to have a longer lived refresh token duration.
     * </p>
     *
     */
    private void createConfidentialTestClient(RegisteredClientRepository registeredClientRepository){
        RegisteredClient confidentialClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("quotes")
                        .clientSecret("{noop}abc123")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri("http://127.0.0.1:8080/login/oauth2/code/demoAuthServer")
                        .redirectUri("http://127.0.0.1:8080/")
                        .redirectUri("http://localhost:8080/login/oauth2/code/demoAuthServer")
                        .redirectUri("http://localhost:8080/")
                        .scope(OidcScopes.OPENID)
                        .scope("quotes.read")
                        // .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                        .build();

        registeredClientRepository.save(confidentialClient);
    }

    /**
     * <p>
     *  The OIDC and oAuth specs define a public client as a client that can securely store
     *  token issues by the auth server and can authenticate itself to the authorization server. Two
     *  common types of public clients are mobile apps and web apps.
     * </p>
     * <p>
     * JavaScript Single Page apps running in a web browser are a typical example of a public client.
     * For example an Angular application running an browser does not have access to a key vault
     * where it can store the access token and refresh token.
     * </p>
     *
     * <p>
     * An iphone or android mobile application published to an app store can't securely store a client
     * secret that it can use to authenticate with the authorization server. Anyone can download the app binary
     * from the app store, decompile it, and find the client-secret. A mobile app has the ability to
     * store a secret in the phone's secure keychain but access to the keychain is not possible until after
     * the user installs the app and starts it.
     * </p>
     *
     * <p>
     * A mobile app can open a webview and run to allow the user to log into the auth server and upgrade itself
     * to a confidential client by using dynamic client registration protocol. Or it can use a device flow to
     * get allow teh user to log in another device.
     * </p>
     *
     * <p><b>Spring Authorization Server does not support issuing refresh tokens to public clients</b></p>
     *
     */
    private void createPublicTestClient(RegisteredClientRepository registeredClientRepository){
        RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("public-client")
                .tokenSettings(TokenSettings.builder().accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED).build())
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://127.0.0.1:4200")
                .redirectUri("http://localhost:4200") // this does not work localhost not allowed see https://github.com/spring-projects/spring-authorization-server/issues/680
                .redirectUri("http://127.0.0.1:4200/silent-renew.html")
                .scope(OidcScopes.OPENID)
                .scope("quotes.read")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).requireProofKey(true).build())
                .build();

        registeredClientRepository.save(publicClient);
    }
}
