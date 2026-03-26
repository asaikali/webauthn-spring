package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class ParClientSecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, OAuth2AuthorizationRequestResolver authorizationRequestResolver)
      throws Exception {
    http.authorizeHttpRequests(
        authorize ->
            authorize
                .requestMatchers(
                    "/login",
                    "/login-error.html",
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**")
                .permitAll()
                .anyRequest()
                .authenticated());
    http.exceptionHandling(
        handling ->
            handling.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint(
                    OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                        + "/demoAuthServer")));
    http.oauth2Login(
        oauth2 ->
            oauth2
                .authorizationEndpoint(
                    authorization ->
                        authorization.authorizationRequestResolver(authorizationRequestResolver))
                .failureUrl("/login-error.html"));
    return http.build();
  }

  @Bean
  OAuth2AuthorizationRequestResolver authorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    return new ParOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
  }
}
