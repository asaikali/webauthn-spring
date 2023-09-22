package org.springframework.security.webauthn.rp.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.json.JsonUtils;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.webauthn.rp.WebAuthnException;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAssertionRequestAuthenticationToken;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAssertionResponseAuthenticationToken;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnAssertionResponse;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

public final class WebAuthnLoginFilter extends OncePerRequestFilter {
    private static final String DEFAULT_LOGIN_START_ENDPOINT_URI = "/webauthn/login";
    private static final String DEFAULT_LOGIN_FINISH_ENDPOINT_URI = "/webauthn/login/finish";
    private static final String ASSERTION_REQUEST_ATTRIBUTE = "ASSERTION_REQUEST";
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
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

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
            this.securityContextHolderStrategy.clearContext();
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Failed on WebAuthn login request.", ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    private void sendLoginResponse(HttpServletRequest request, HttpServletResponse response,
                                   Authentication authentication) throws IOException {
        if (authentication instanceof WebAuthnAssertionRequestAuthenticationToken) {
            sendLoginStartResponse(request, response, authentication);
        } else {
            sendLoginFinishResponse(request, response, authentication);
        }
    }

    private void sendLoginStartResponse(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        WebAuthnAssertionRequestAuthenticationToken assertionRequestAuthentication =
                (WebAuthnAssertionRequestAuthenticationToken) authentication;

        WebAuthnAssertionRequest assertionRequest = assertionRequestAuthentication.getAssertionRequest();

        HttpSession session = request.getSession();
        session.setAttribute(ASSERTION_REQUEST_ATTRIBUTE, assertionRequest.getAssertionRequest());

        jsonMessageConverter.write(assertionRequest, WebAuthnAssertionRequest.class,
                MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

    private void sendLoginFinishResponse(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        WebAuthnAssertionResponseAuthenticationToken assertionResponseAuthentication =
                (WebAuthnAssertionResponseAuthenticationToken) authentication;

        AssertionResult assertionResult = assertionResponseAuthentication.getAssertionResult();

        HttpSession session = request.getSession();
        session.removeAttribute(ASSERTION_REQUEST_ATTRIBUTE);

        if (assertionResult.isSuccess()) {
            SecurityContext securityContext = this.securityContextHolderStrategy.createEmptyContext();
            securityContext.setAuthentication(assertionResponseAuthentication);
            this.securityContextHolderStrategy.setContext(securityContext);
            this.securityContextRepository.saveContext(securityContext, request, response);
        }

        jsonMessageConverter.write(assertionResult, AssertionResult.class,
                MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Failed on WebAuthn login request.", exception);
        }
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    private static final class WebAuthnLoginStartAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_LOGIN_START_ENDPOINT_URI)) {
                return null;
            }

            WebAuthnAssertionCreateRequest assertionCreateRequest;
            try {
                assertionCreateRequest = (WebAuthnAssertionCreateRequest) jsonMessageConverter.read(
                        WebAuthnAssertionCreateRequest.class, null, new ServletServerHttpRequest(request));
            } catch (IOException ex) {
                throw new WebAuthnException("Malformed WebAuthn login start request.", ex);
            }

            return new WebAuthnAssertionRequestAuthenticationToken(assertionCreateRequest);
        }

    }

    private static final class WebAuthnLoginFinishAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_LOGIN_FINISH_ENDPOINT_URI)) {
                return null;
            }

            AssertionRequest assertionRequest = (AssertionRequest) request.getSession().getAttribute(ASSERTION_REQUEST_ATTRIBUTE);
            if (assertionRequest == null) {
                throw new WebAuthnException("WebAuthn login start request not found.");
            }

            String username = request.getParameter("username");
            String finishRequest = request.getParameter("finishRequest");
            WebAuthnAssertionResponse assertionResponse = JsonUtils.fromJson(finishRequest, WebAuthnAssertionResponse.class);

            return new WebAuthnAssertionResponseAuthenticationToken(username, assertionResponse);
        }

    }

    private static final class DelegatingAuthenticationConverter implements AuthenticationConverter {
        private final List<AuthenticationConverter> converters;

        private DelegatingAuthenticationConverter(List<AuthenticationConverter> converters) {
            Assert.notEmpty(converters, "converters cannot be empty");
            this.converters = Collections.unmodifiableList(new LinkedList<>(converters));
        }

        @Nullable
        @Override
        public Authentication convert(HttpServletRequest request) {
            Assert.notNull(request, "request cannot be null");
            for (AuthenticationConverter converter : this.converters) {
                Authentication authentication = converter.convert(request);
                if (authentication != null) {
                    return authentication;
                }
            }
            return null;
        }

    }

}