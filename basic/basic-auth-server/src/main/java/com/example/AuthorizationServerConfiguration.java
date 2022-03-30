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
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfiguration {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    return http.formLogin(Customizer.withDefaults()).build();
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    RSAKey rsaKey = Jwks.generateRsa();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

  /**
   * only needed for the UserInfoEndpoint and dynamic client registeration.
   *
   * @return
   */
  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

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
   *
   * The spring authorization server ships with 2 implementations of RegisteredClientRepository:
   * InMemoryRegisteredClientRepository suitable for tests and sample apps, and
   * JdbcRegisteredClientRepository which can be used in production with a SQL database provide you
   * like the schema that it excepts.
   *
   * In a real world scenario you likely want to provide a custom implementation of
   * RegisteredClientRepository to meet your specific needs.
   *
   * @return The client repository that will be used by the auth server to determine how it should
   * interact with a client application that wants to use the auth server.
   *
   */
  @Bean
  public RegisteredClientRepository registeredClientRepository() {

    RegisteredClient registeredClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("quotes")
            .clientSecret("{noop}abc123")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://127.0.0.1:8080/login/oauth2/code/demoAuthServer")
            .redirectUri("http://127.0.0.1:8080/")
            .scope(OidcScopes.OPENID)
            .scope("read")
            // .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .build();

    return new InMemoryRegisteredClientRepository(registeredClient);
  }
}
