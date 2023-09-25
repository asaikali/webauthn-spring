package org.springframework.security.webauthn.rp.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.webauthn.rp.WebAuthnException;
import org.springframework.security.webauthn.rp.WebAuthnRegistrationService;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationCreateRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationRequest;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationResponse;
import org.springframework.security.webauthn.rp.data.WebAuthnRegistrationSuccessResponse;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

public final class WebAuthnRegistrationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_REGISTRATION_START_ENDPOINT_URI = "/webauthn/register";
    private static final String DEFAULT_REGISTRATION_FINISH_ENDPOINT_URI = "/webauthn/register/finish";
    private static final String REGISTRATION_REQUEST_ATTRIBUTE = "REGISTRATION_REQUEST";
    private static final GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();
    private final WebAuthnRegistrationService registrationService;
    private final RequestMatcher registrationStartEndpointMatcher;
    private final RequestMatcher registrationFinishEndpointMatcher;

    public WebAuthnRegistrationFilter(WebAuthnRegistrationService registrationService) {
        Assert.notNull(registrationService, "registrationService cannot be null");
        this.registrationService = registrationService;
        this.registrationStartEndpointMatcher = new AntPathRequestMatcher(
                DEFAULT_REGISTRATION_START_ENDPOINT_URI, HttpMethod.POST.name());
        this.registrationFinishEndpointMatcher = new AntPathRequestMatcher(
                DEFAULT_REGISTRATION_FINISH_ENDPOINT_URI, HttpMethod.POST.name());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (this.registrationStartEndpointMatcher.matches(request)) {
            startRegistration(request, response);
        } else if (this.registrationFinishEndpointMatcher.matches(request)) {
            finishRegistration(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void startRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebAuthnRegistrationCreateRequest registrationCreateRequest;
        try {
            registrationCreateRequest = (WebAuthnRegistrationCreateRequest) jsonMessageConverter.read(
                    WebAuthnRegistrationCreateRequest.class, null, new ServletServerHttpRequest(request));
        } catch (IOException ex) {
            throw new WebAuthnException("Malformed WebAuthn registration create request.", ex);
        }

        WebAuthnRegistrationRequest registrationRequest;
        try {
            registrationRequest = this.registrationService.startRegistration(registrationCreateRequest);
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to start WebAuthn registration.", ex);
        }

        HttpSession session = request.getSession();
        session.setAttribute(REGISTRATION_REQUEST_ATTRIBUTE, registrationRequest);

        jsonMessageConverter.write(registrationRequest, WebAuthnRegistrationRequest.class,
                MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

    private void finishRegistration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebAuthnRegistrationResponse registrationResponse;
        try {
            registrationResponse = (WebAuthnRegistrationResponse) jsonMessageConverter.read(
                    WebAuthnRegistrationResponse.class, null, new ServletServerHttpRequest(request));
        } catch (IOException ex) {
            throw new WebAuthnException("Malformed WebAuthn registration response.", ex);
        }

        HttpSession session = request.getSession();
        WebAuthnRegistrationRequest registrationRequest = (WebAuthnRegistrationRequest) session.getAttribute(REGISTRATION_REQUEST_ATTRIBUTE);
        if (registrationRequest == null) {
            throw new WebAuthnException("WebAuthn registration request not found.");
        }

        WebAuthnRegistrationSuccessResponse registrationSuccessResponse;
        try {
            registrationSuccessResponse = this.registrationService.finishRegistration(
                    registrationRequest, registrationResponse);
        } catch (Exception ex) {
            throw new WebAuthnException("Failed to finish WebAuthn registration.", ex);
        }

        session.removeAttribute(REGISTRATION_REQUEST_ATTRIBUTE);

        jsonMessageConverter.write(registrationSuccessResponse, WebAuthnRegistrationSuccessResponse.class,
                MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));

    }

}