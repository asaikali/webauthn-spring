package com.example;

import com.example.util.Jwks;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://127.0.0.1:4200");
        config.setAllowCredentials(false);
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * The authorization server defines a set of api endpoints that are invoked by client
     * applications following the rules of the OpenID Connect and OAuth2 protocols. The auth
     * server endpoints have their own security rules that must be enforced. Enforcement is done
     * by creating a SecurityFilterChain and configuring it correctly.  To make things easy
     * the Spring Auth server provides a helper method to apply the default configuration
     * for security to the end points.
     * <p>
     * The @Order anotation is used here because ??
     */
    @Bean
    @Order(0)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        return http.cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2ResourceServer -> {
                    oauth2ResourceServer.jwt();
                })
                .formLogin(Customizer.withDefaults()).build();
    }

    /**
     * The authorization server produces JWT tokens that are digitally signed. So the
     * auth server needs public-private key pair to use sign JWT tokens that it issues.
     * This method is used to provides a list of key pairs for JWT token signing.
     * <p>
     * In this sample application we are generating a key pair everytime the auth server
     * starts. However, in a real world scenario you will need to store the auth server
     * sigining keys in a secure location such as a key vault.
     *
     * @return
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * The authorization server has two protected endpoints /userinfo and /clientregisteration
     * the /userinfo provides  info about the user and
     * only needed for the UserInfoEndpoint and dynamic client registeration.
     * why?
     * <p>
     * not needed if this is using opaque access tokens.
     *
     * @return
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * What is provider settings and why do we need it? Is the provider setting used to
     * generate the /.well-known/openid-configuration object and this is why we need
     * this section?
     * <p>
     * <p>
     * This usess all the default settings.
     *
     * @return
     */
    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }

    /**
     * The authorization server only talks to applications it knows about. The auth server
     * keeps track of a known application using a RegisteredClient object. The Registered client
     * object needs to be persisted in database but the auth server does not want to dictate a
     * specific type of database to use. So the auth server defines an interface called
     * RegisteredClientRepository which is used to save and load details of apps that the
     * auth server is allowed to talk to.
     * <p>
     * The spring authorization server ships with 2 implementations of RegisteredClientRepository:
     * InMemoryRegisteredClientRepository suitable for tests and sample apps, and
     * JdbcRegisteredClientRepository which can be used in production with a SQL database provide you
     * like the schema that it excepts.
     * <p>
     * In a real world scenario you likely want to provide a custom implementation of
     * RegisteredClientRepository to meet your specific needs.
     *
     * @return The client repository that will be used by the auth server to determine how it should
     * interact with a client application that wants to use the auth server.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

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
                        //   .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).requireProofKey(true).build()) PKCE comming spring security 5.7
                        .scope(OidcScopes.OPENID)
                        .scope("quotes.read")
                        // .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                        .build();

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
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).requireProofKey(true).build())
                .build();
        return new InMemoryRegisteredClientRepository(confidentialClient, publicClient);
    }
}
