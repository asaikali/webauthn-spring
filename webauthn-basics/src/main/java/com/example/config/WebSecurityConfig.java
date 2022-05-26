package com.example.config;

import com.example.security.webauthn.login.FidoLoginSuccessHandler;
import com.example.security.webauthn.login.FidoAuthenticationConverter;
import com.example.security.webauthn.login.FidoAuthenticationManager;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * This class uses the Lambda style DSL see the following blog posts for more info
 * <p>
 * https://spring.io/blog/2019/11/21/spring-security-lambda-dsl
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, FidoAuthenticationManager fidoAuthenticationManager) throws Exception {

        // for education purposes we turn on the h2-console so we need to make sure that
        // spring security does not block requests to the h2 console.
        //
        // WARNING: NEVER do this in production
        http.csrf().ignoringAntMatchers("/h2-console/**");
        http.headers().frameOptions().sameOrigin();
        http.authorizeRequests().antMatchers("/h2-console/**").permitAll();

        // requires all communications to be over tls since webauthn does not work if http is used
        http.requiresChannel().anyRequest().requiresSecure();

        // configure url authorization rules
        http.authorizeRequests(authorizeRequests ->
                authorizeRequests
                        .mvcMatchers("/",
                                "/register",
                                "/webauthn/login/start",
                                "/webauthn/login/finish",
                                "/webauthn/register/start",
                                "/webauthn/register/finish",
                                "/webauthn/login",
                                "favicon.ico").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated());

        // uncomment the code below and the userdetail service if you want to have a mix of login methods
//        http.formLogin(formLogin ->
//                formLogin.defaultSuccessUrl("/quotes")
//        );

        // Configure a generic authentication filter that knows how to log in a user using a fido authentication token
        // the key thing about this code is the convertor which can take a http request and extract out the fido
        // credential and the authentication manager that validates the fido credential.
        // the success handler defined below is for debug purposes so that we can see the full flow of interaction
        // between the browser and the fido server that we are implementing in this sample normally you would configure
        // success handler to go to a url after successfully logging in.
        var authenticationFilter = new AuthenticationFilter(fidoAuthenticationManager, new FidoAuthenticationConverter());
        authenticationFilter.setRequestMatcher( new AntPathRequestMatcher("/fido/login"));
        authenticationFilter.setSuccessHandler( new FidoLoginSuccessHandler());
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    public UserDetailsService userDetailsService() {
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        User.UserBuilder users = User.withDefaultPasswordEncoder();
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
