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

import com.example.security.fido.register.RegistrationFinishRequest;
import com.example.security.fido.register.RegistrationFinishResponse;
import com.example.security.fido.register.RegistrationStartRequest;
import com.example.security.fido.register.RegistrationStartResponse;

import org.springframework.core.log.LogMessage;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAuthenticationException;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAuthenticatorAttestationAuthenticationToken;
import org.springframework.security.webauthn.rp.authentication.WebAuthnCredentialCreationAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

public final class WebAuthnRegistrationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_REGISTRATION_START_ENDPOINT_URI = "/webauthn/register/start";
    private static final String DEFAULT_REGISTRATION_FINISH_ENDPOINT_URI = "/webauthn/register/finish";
    private static final String START_REG_REQUEST = "start_reg_request";
    private static final GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher registrationEndpointMatcher;
    private final AuthenticationConverter authenticationConverter = new DelegatingAuthenticationConverter(
            Arrays.asList(
                    new WebAuthnRegistrationStartAuthenticationConverter(),
                    new WebAuthnRegistrationFinishAuthenticationConverter()
            )
    );
    private final AuthenticationSuccessHandler authenticationSuccessHandler = this::sendRegistrationResponse;
    private final AuthenticationFailureHandler authenticationFailureHandler = this::sendErrorResponse;

    public WebAuthnRegistrationFilter(AuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.authenticationManager = authenticationManager;
        this.registrationEndpointMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(DEFAULT_REGISTRATION_START_ENDPOINT_URI, HttpMethod.POST.name()),
                new AntPathRequestMatcher(DEFAULT_REGISTRATION_FINISH_ENDPOINT_URI, HttpMethod.POST.name())
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!this.registrationEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication registrationAuthentication = this.authenticationConverter.convert(request);
            Authentication registrationAuthenticationResult = this.authenticationManager.authenticate(registrationAuthentication);
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, registrationAuthenticationResult);
        } catch (WebAuthnAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("WebAuthn Registration request failed: %s", ex.getMessage()), ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }

    }

    private void sendRegistrationResponse(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException {
        if (authentication instanceof WebAuthnCredentialCreationAuthenticationToken) {
            sendRegistrationStartResponse(request, response, authentication);
        } else {
            sendRegistrationFinishResponse(request, response, authentication);
        }
    }

    private void sendRegistrationStartResponse(HttpServletRequest request, HttpServletResponse response,
                                               Authentication authentication) throws IOException {
        WebAuthnCredentialCreationAuthenticationToken credentialCreationAuthentication =
                (WebAuthnCredentialCreationAuthenticationToken) authentication;

        RegistrationStartResponse registrationStartResponse = credentialCreationAuthentication.getRegistrationStartResponse();

        HttpSession session = request.getSession();
        session.setAttribute(START_REG_REQUEST, registrationStartResponse);

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        jsonMessageConverter.write(registrationStartResponse, RegistrationStartResponse.class,
                MediaType.APPLICATION_JSON, httpResponse);
    }

    private void sendRegistrationFinishResponse(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException {
        WebAuthnAuthenticatorAttestationAuthenticationToken authenticatorAttestationAuthentication =
                (WebAuthnAuthenticatorAttestationAuthenticationToken) authentication;

        RegistrationFinishResponse registrationFinishResponse = authenticatorAttestationAuthentication.getRegistrationFinishResponse();

        HttpSession session = request.getSession();
        session.removeAttribute(START_REG_REQUEST);

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        jsonMessageConverter.write(registrationFinishResponse, RegistrationFinishResponse.class,
                MediaType.APPLICATION_JSON, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    private static final class WebAuthnRegistrationStartAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_REGISTRATION_START_ENDPOINT_URI)) {
                return null;
            }

            ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);

            RegistrationStartRequest registrationStartRequest;
            try {
                registrationStartRequest = (RegistrationStartRequest) jsonMessageConverter.read(RegistrationStartRequest.class, null, httpRequest);
            } catch (IOException ex) {
                throw new WebAuthnAuthenticationException("WebAuthn Registration start request conversion failed.", ex);
            }

            return new WebAuthnCredentialCreationAuthenticationToken(registrationStartRequest);
        }

    }

    private static final class WebAuthnRegistrationFinishAuthenticationConverter implements AuthenticationConverter {

        @Override
        public Authentication convert(HttpServletRequest request) {
            if (!request.getRequestURI().equals(DEFAULT_REGISTRATION_FINISH_ENDPOINT_URI)) {
                return null;
            }

            ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);

            RegistrationFinishRequest registrationFinishRequest;
            try {
                registrationFinishRequest = (RegistrationFinishRequest) jsonMessageConverter.read(RegistrationFinishRequest.class, null, httpRequest);
            } catch (IOException ex) {
                throw new WebAuthnAuthenticationException("WebAuthn Registration finish request conversion failed.", ex);
            }

            RegistrationStartResponse registrationStartResponse = (RegistrationStartResponse) request.getSession().getAttribute(START_REG_REQUEST);
            if (registrationStartResponse == null) {
                throw new WebAuthnAuthenticationException("WebAuthn Registration start request not found.");
            }

            return new WebAuthnAuthenticatorAttestationAuthenticationToken(registrationFinishRequest,
                    registrationStartResponse.getCredentialCreationOptions());
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