package com.example.security.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.webauthn.rp.config.annotation.web.configurers.WebAuthnRelyingPartyConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * This class uses the Lambda style DSL see the following blog posts for more info
 *
 * https://spring.io/blog/2019/11/21/spring-security-lambda-dsl
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  /*
   * see https://github.com/jzheaux/cve-2023-34035-mitigations/tree/main#mitigations
   */
  @Bean
  MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
    return new MvcRequestMatcher.Builder(introspector);
  }

  @Bean
  SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {

    // for education purposes we turn on the h2-console so we need to make sure that
    // spring security does not block requests to the h2 console.
    //
    // WARNING: NEVER do this in production
    http.csrf().ignoringRequestMatchers(PathRequest.toH2Console());
    http.headers().frameOptions().sameOrigin();
    http.authorizeHttpRequests().requestMatchers(PathRequest.toH2Console()).permitAll();

    // requires all communications to be over tls since webauthn does not work if http is used
    http.requiresChannel().anyRequest().requiresSecure();

    // configure url authorization rules
    http.authorizeHttpRequests().requestMatchers(
                    mvc.pattern("/"),
                    mvc.pattern("/register"),
                    mvc.pattern("/webauthn/login"),
                    mvc.pattern("favicon.ico"))
                .permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .permitAll()
                .anyRequest()
                .authenticated();

    // uncomment the code below and the userdetail service if you want to have a mix of login
    // methods
    //        http.formLogin(formLogin ->
    //                formLogin.defaultSuccessUrl("/quotes")
    //        );

    http.oauth2Client(Customizer.withDefaults());
    http.apply(new WebAuthnRelyingPartyConfigurer());

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
  //        UserDetails admin = users.username("admin").password("admin").roles("USER",
  // "ADMIN").build();
  //        manager.createUser(admin);
  //
  //        return manager;
  //    }

}
