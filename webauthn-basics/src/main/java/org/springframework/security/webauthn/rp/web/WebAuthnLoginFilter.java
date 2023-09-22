package org.springframework.security.webauthn.rp.web;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.json.JsonUtils;
import com.example.security.fido.login.LoginFinishRequest;
import com.example.security.fido.login.LoginStartRequest;
import com.example.security.fido.login.LoginStartResponse;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;

import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.webauthn.rp.WebAuthnException;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAuthenticatorAssertionAuthenticationToken;
import org.springframework.security.webauthn.rp.authentication.WebAuthnCredentialRequestAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

public final class WebAuthnLoginFilter extends OncePerRequestFilter {
    private static final String DEFAULT_LOGIN_START_ENDPOINT_URI = "/webauthn/login/start";
    private static final String DEFAULT_LOGIN_FINISH_ENDPOINT_URI = "/webauthn/login/finish";
    private static final String START_LOGIN_REQUEST = "start_login_request";
    private static final GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher loginEndpointMatcher;
    private final AuthenticationConverter authenticationConverter = new DelegatingAuthenticationConverter(
            Arrays.asList(
                    new WebAuthnLoginStartAuthenticationConverter(),
                    new WebAuthnLoginFinishAuthenticationConverter()
            )
    );
    private final AuthenticationSuccessHandler authenticationSuccessHandler = this::sendLoginResponse;
    private final AuthenticationFailureHandler authenticationFailureHandler = this::sendErrorResponse;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public WebAuthnLoginFilter(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.authenticationManager = authenticationManager;
        this.loginEndpointMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(DEFAULT_LOGIN_START_ENDPOINT_URI, HttpMethod.POST.name()),
                new AntPathRequestMatcher(DEFAULT_LOGIN_FINISH_ENDPOINT_URI, HttpMethod.POST.name())
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!this.loginEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication loginAuthentication = this.authenticationConverter.convert(request);
            Authentication loginAuthenticationResult = this.authenticationManager.authenticate(loginAuthentication);
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, loginAuthenticationResult);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("WebAuthn Login request failed: %s", ex.getMessage()), ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }

    }

    private void sendLoginResponse(HttpServletRequest request, HttpServletResponse response,
                                   Authentication authentication) throws IOException {
        if (authentication instanceof WebAuthnCredentialRequestAuthenticationToken) {
            sendLoginStartResponse(request, response, authentication);
        } else {
            sendLoginFinishResponse(request, response, authentication);
        }
    }

    private void sendLoginStartResponse(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        WebAuthnCredentialRequestAuthenticationToken credentialRequestAuthentication =
                (WebAuthnCredentialRequestAuthenticationToken) authentication;

        LoginStartResponse loginStartResponse = credentialRequestAuthentication.getLoginStartResponse();

        HttpSession session = request.getSession();
        session.setAttribute(START_LOGIN_REQUEST, loginStartResponse.getAssertionRequest());

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        jsonMessageConverter.write(loginStartResponse, LoginStartResponse.class,
                MediaType.APPLICATION_JSON, httpResponse);
    }

    private void sendLoginFinishResponse(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        WebAuthnAuthenticatorAssertionAuthenticationToken authenticatorAssertionAuthentication =
                (WebAuthnAuthenticatorAssertionAuthenticationToken) authentication;

        AssertionResult assertionResult = authenticatorAssertionAuthentication.getAssertionResult();

        HttpSession session = request.getSession();
        session.removeAttribute(START_LOGIN_REQUEST);
        if (assertionResult.isSuccess()) {
            session.setAttribute(AssertionRequest.class.getName(), assertionResult);

            // FIXME Save another type of Authentication, e.g. Fido2Authentication
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticatorAssertionAuthentication);
            SecurityContextHolder.setContext(securityContext);
            this.securityContextRepository.saveContext(securityContext, request, response);
        }

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        jsonMessageConverter.write(assertionResult, AssertionResult.class,
                MediaType.APPLICATION_JSON, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    private static final class WebAuthnLoginStartAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_LOGIN_START_ENDPOINT_URI)) {
                return null;
            }

            ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);

            LoginStartRequest loginStartRequest;
            try {
                loginStartRequest = (LoginStartRequest) jsonMessageConverter.read(LoginStartRequest.class, null, httpRequest);
            } catch (IOException ex) {
                throw new WebAuthnException("WebAuthn Login start request conversion failed.", ex);
            }

            return new WebAuthnCredentialRequestAuthenticationToken(loginStartRequest);
        }

    }

    private static final class WebAuthnLoginFinishAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_LOGIN_FINISH_ENDPOINT_URI)) {
                return null;
            }

            AssertionRequest assertionRequest = (AssertionRequest) request.getSession().getAttribute(START_LOGIN_REQUEST);
            if (assertionRequest == null) {
                throw new WebAuthnException("WebAuthn Login start request not found.");
            }

            String username = request.getParameter("username");
            String finishRequest = request.getParameter("finishRequest");
            LoginFinishRequest loginFinishRequest = JsonUtils.fromJson(finishRequest, LoginFinishRequest.class);

            return new WebAuthnAuthenticatorAssertionAuthenticationToken(username, loginFinishRequest);
        }

    }

}