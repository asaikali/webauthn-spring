package com.example.moneymate.protectedtestapi.security;

import com.example.moneymate.protectedtestapi.config.IdentityBrokerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Clock;

@Configuration
public class SecurityConfig {

    private static final String REQUIRED_SCOPE = "hypermedia.access";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            IdentityBrokerProperties identityBrokerProperties,
                                            AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, "/", "/AGENTS.md", "/.well-known/oauth-protected-resource", "/problems/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/protected").hasAuthority("SCOPE_" + REQUIRED_SCOPE)
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(authenticationEntryPoint)
                .protectedResourceMetadata(metadata -> metadata.protectedResourceMetadataCustomizer(builder -> builder
                    .authorizationServer(identityBrokerProperties.getIssuerUri())
                    .scope(REQUIRED_SCOPE)))
            )
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return new ProblemDetailsAuthenticationEntryPoint();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
