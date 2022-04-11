package com.example.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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


        http.authorizeRequests(authorizeRequests ->
                authorizeRequests
                        .mvcMatchers("/", "/register", "/users/register/start",  "/users/register/finish", "favicon.ico").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated());

        http.formLogin(formLogin ->
                formLogin
                        .loginPage("/login")
                        .permitAll()
        );

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
