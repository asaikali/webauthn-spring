package com.example.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 *
 * <p>Start by reading the Javadoc on the SecurityFilterChainConfig class if you have not read it already</p>
 *
 * <p>
 * Spring Security protects web endpoints by sending a request through a security filter chain. Each filter
 * in the filter chain inspects the HTTP request and applies a policy to decide if the request
 * should be allowed or rejected.
 * <p>
 *
 * <p>
 * The spring authorization server exposes a set of URLs that implement the functionality of various
 * protocols such as OpenIDConnect, OAuth2 that are implemented by the authorization server. The default
 * urls can be found in the ProviderSettings Builder
 * </p>
 * <pre>
 * public static Builder builder() {
 * return new Builder()
 *  .authorizationEndpoint("/oauth2/authorize")
 *  .tokenEndpoint("/oauth2/token")
 *  .jwkSetEndpoint("/oauth2/jwks")
 *  .tokenRevocationEndpoint("/oauth2/revoke")
 *  .tokenIntrospectionEndpoint("/oauth2/introspect")
 *  .oidcClientRegistrationEndpoint("/connect/register")
 *  .oidcUserInfoEndpoint("/userinfo");
 * </pre>
 *
 * <p>
 * There are very specific security rules that each of the protocol endpoints above must implement. The logic for
 * the endpoints is implemented in Spring security filters, that are arranged into a specific security filter chain.
 * Spring Authorization Server provides a handy utility method to create and configure a SecurityFilterChain.
 * </p>
 *
 * <p>
 * Spring security offers capabilities for logging users into a single application, and leverage multiple common
 * authentication technologies such as ActiveDirectory, OpenIDConnect login, SAML Login, LDAP Login, or custom user
 * database login. The Spring Authorization server assumes that you have configured a SecurityFilterChain to handle
 * authenticating a user to the spring boot app hosting the authorization server end points.
 * </p>
 *
 * <p>
 * So we need two independent SecurityFilterChain configuration to co-exist in the same SpringBoot application. Spring
 * Security supports the concept of multiple security filter chains, but is important to put those chains in specific
 * order since a request will only be processed a by a single security chain. The first chain that returns true when
 * asked it it should handle the request. See
 * <a href="https://docs.spring.io/spring-security/site/docs/5.5.5/reference/html5/images/servlet/architecture/multi-securityfilterchain.png">Architecture diagarm</a>
 * from spring security.
 * </p>
 *
 * <p>The code in this class configures two SecurityFilterChains one for authenticating users to the authorization
 * server and one for implementing the security protocols supported by the authorization server.</p>
 *
 * <p>
 * There are multiple ways that can be used to configure spring security, this classes uses the most recently
 * recommend way from the spring team using the Lambda DSL. Read the following blog posts if you are not
 * familiar with this style of configuration:
 * <a href="https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter">Spring Security without the WebSecurityConfigurerAdapter</a>
 * <p/>,
 * <a href="https://spring.io/blog/2019/11/21/spring-security-lambda-dsl">Spring Security - Lambda DSL</a>
 *
 * @see org.springframework.security.oauth2.server.authorization.config.ProviderSettings.Builder
 */
@EnableWebSecurity
@Configuration
public class SecurityFilterChainConfig {

    /**
     * <p>
     * The authorization server defines a set of api endpoints that are invoked by client
     * applications following the rules of the OpenID Connect and OAuth2 protocols. The auth
     * server endpoints have their own security rules that must be enforced. Enforcement is done
     * by creating a SecurityFilterChain and configuring it correctly.  To make things easy
     * the Spring Auth server provides a helper method to apply the default configuration
     * for security to the end points.
     * <p>
     *
     * <p>
     * We want the security filter chain we are defining here to be the first filter chain to
     * inspect an HTTP request, this ensures that proper protocol implementation happens. If we don't
     * have this filter chain run first, it is possible another filter chain will match on all
     * urls because it configured to match /** pattern thus causing the authorization server protocol
     * endpoint logic to never execute.
     * </p>
     *
     * <p>
     * When there are multiple SecurityFilterChain beans spring security will put them into a list sorted by
     * numerical value set in the Order annotation. This is why we define authorization server with the
     * highest possible Order.
     * </p>
     *
     * @param http builder pojo used to configure the security filter chain
     */
    @Bean(name="auth-server")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // apply the default configuration shipped with the authorization server
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Enable OpenID Connect on the Authorization Server
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        // configure cross-origin request so that angular app can make calls to the auth server
        http.cors(Customizer.withDefaults());

        // OpenID connect /userinfo endpoint can be called with an access token and it returns
        // a JWT with information about the user. The /userinfo end requires the caller to provide
        // an access token, so the auth server reuses the spring security resource server support
        // to enforce security on the /userinfo end point
        http.oauth2ResourceServer().jwt();

        // allows client apps to authenticate with the auth server using http basic authentication via client-id and
        // client-secret
        http.formLogin(Customizer.withDefaults());

        // build the security filter chain and return it
        return http.build();
    }

    /**
     * <p>
     * Configure the filter security chain that will be used to authenticate users. This filter chain
     * will always run after the authorization server filter chain. In a real world scenario this
     * filter chain is configured to perform authentication using a real world technology such as
     * Active Directory, A custom user database, a social login provider such as google or linkedIn.
     * </p>
     *
     * <p>
     * The application hosting the auth server needs to be able to authenticate client application and
     * users. authentication of application client is done by the authorization server security filter
     * chain. The authentication of end users is done by this filter chain.
     * </p>
     *
     * <p>
     * We are putting the ordered annotation for consistent, even if we remove the annotation things will
     * continue to work, but we want to explicitly call out that there is an Order that we care about.
     * </p>
     * @param http
     * @return
     * @throws Exception
     */
    @Bean(name="webapp")
    @Order(Ordered.LOWEST_PRECEDENCE)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // configure cross-origin request so that angular app can make calls to the auth server
        http.cors(Customizer.withDefaults());

        // for education purposes we turn on the h2-console so we need to make sure that
        // spring security does not block requests to the h2 console.
        //
        // WARNING: NEVER do this in production
        http.csrf().ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"));
        http.headers().frameOptions().sameOrigin();
        http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll();

        // set default request policy to required authentication.
        http.authorizeHttpRequests().anyRequest().authenticated();

        // define a user detail service that uses some hard coded test users
        // WARNING: NEVER do this in production
        http.userDetailsService(this.userDetailsService());

        // turn on form authentication
        http.formLogin(Customizer.withDefaults());

        // build the security filter chain and return it
        return http.build();
    }

    /**
     * create a test user details for testing purposes. don't do this in production, implement
     * a real user detail service or AuthenticationManager.
     */
    private UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        User.UserBuilder users = User.withDefaultPasswordEncoder();

        // add a regular user
        UserDetails user = users.username("user").password("user").roles("USER").build();
        manager.createUser(user);

        // add an admin user
        UserDetails admin = users.username("admin").password("admin").roles("USER", "ADMIN").build();
        manager.createUser(admin);

        return manager;
    }

}
