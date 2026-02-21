package com.example.security.config;

import com.example.security.fido.login.FidoAuthenticationConverter;
import com.example.security.fido.login.FidoAuthenticationManager;
import com.example.security.fido.login.FidoLoginSuccessHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
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
      HttpSecurity http, FidoAuthenticationManager fidoAuthenticationManager,MvcRequestMatcher.Builder mvc) throws Exception {

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
                    mvc.pattern("/webauthn/login/start"),
                    mvc.pattern("/webauthn/login/finish"),
                    mvc.pattern("/webauthn/register/start"),
                    mvc.pattern("/webauthn/register/finish"),
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

    // Configure a generic authentication filter that knows how to log in a user using a fido
    // authentication token
    // the key thing about this code is the convertor which can take a http request and extract out
    // the fido
    // credential and the authentication manager that validates the fido credential.
    // the success handler defined below is for debug purposes so that we can see the full flow of
    // interaction
    // between the browser and the fido server that we are implementing in this sample normally you
    // would configure
    // success handler to go to a url after successfully logging in.

    // http.securityContext().requireExplicitSave(false);

    var authenticationFilter =
        new AuthenticationFilter(fidoAuthenticationManager, new FidoAuthenticationConverter());
    authenticationFilter.setRequestMatcher(new AntPathRequestMatcher("/fido/login"));
    authenticationFilter.setSuccessHandler(new FidoLoginSuccessHandler());
    authenticationFilter.setSecurityContextRepository( new HttpSessionSecurityContextRepository());
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
  //        UserDetails admin = users.username("admin").password("admin").roles("USER",
  // "ADMIN").build();
  //        manager.createUser(admin);
  //
  //        return manager;
  //    }

}
