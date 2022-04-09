package com.example;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorizeRequests -> {

                    authorizeRequests.antMatchers("/h2-console/**").permitAll()
                            .anyRequest().authenticated();
                })
                .cors(Customizer.withDefaults())
                .csrf().ignoringAntMatchers("/h2-console/**").and()
                .headers().frameOptions().sameOrigin().and()
                .formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        UserBuilder users = User.withDefaultPasswordEncoder();

        // add a regular user
        UserDetails user = users.username("user").password("user").roles("USER").build();
        manager.createUser(user);

        // add an admin user
        UserDetails admin = users.username("admin").password("admin").roles("USER", "ADMIN").build();
        manager.createUser(admin);

        return manager;
    }
}
