package com.example.config;

import com.example.security.webauthn.login.FidoAuthenticationProvider;
import com.example.security.webauthn.login.FidoLoginSuccessHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * This class uses the Lambda style DSL see the following blog posts for more info
 * <p>
 * https://spring.io/blog/2019/11/21/spring-security-lambda-dsl
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        // for education purposes we turn on the h2-console so we need to make sure that
        // spring security does not block requests to the h2 console.
        //
        // WARNING: NEVER do this in production
        http.csrf().ignoringAntMatchers("/h2-console/**");
        http.headers().frameOptions().sameOrigin();
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll();

        http.requiresChannel().anyRequest().requiresSecure();

        http.authorizeRequests(authorizeRequests ->
                authorizeRequests
                        .mvcMatchers("/",
                                "/register",
                                "/users/login/start",
                                "/users/login/finish",
                                "/users/register/start",
                                "/users/register/finish",
                                "favicon.ico").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated());

        http.formLogin(formLogin ->
                formLogin
                        .defaultSuccessUrl("/quotes")
                        .loginPage("/login")
                        .loginProcessingUrl("/fido/login")
                        .successHandler( new FidoLoginSuccessHandler())
                        .permitAll()
        );

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(FidoAuthenticationProvider fidoAuthenticationProvider) {
        return new ProviderManager(List.of( fidoAuthenticationProvider ));
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        UserBuilder users = User.withDefaultPasswordEncoder();
//
//        // add a regular user
//        UserDetails user = users.username("user").password("user").roles("USER").build();
//        manager.createUser(user);
//
//        // add an admin user
//        UserDetails admin = users.username("admin").password("admin").roles("USER", "ADMIN").build();
//        manager.createUser(admin);
//
//        return manager;
//    }

}
