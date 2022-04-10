package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@EnableWebSecurity
public class ResourceServerConfig {


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // configure the required scope for the /random-quote url
        http.authorizeRequests()
                .mvcMatchers("/random-quote").access("hasAuthority('SCOPE_quotes.read')")
                .anyRequest().authenticated();

        // step cross-origin requests so that the angular public client can call this service
        http.cors();

        // configure the resource server to expect opaque tokens which can only be validated
        // my making a call to the auth server and asking the auth server to validate
        // the provided token is actually valid.
        http.oauth2ResourceServer().opaqueToken();

        // return the configured security filter chain
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("http://127.0.0.1:4200");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
