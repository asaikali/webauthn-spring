package com.example.moneymate.protectedtestapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Adds RFC 9457 problem details body while preserving OAuth Bearer challenge semantics.
 */
public final class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String PROBLEM_TYPE_PATH = "/problems/authentication-required";
    private final BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException {
        delegate.commence(request, response, authException);

        String typeUri = absoluteUri(request, PROBLEM_TYPE_PATH);

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(problemJson(typeUri));
    }

    private static String absoluteUri(HttpServletRequest request, String path) {
        return UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
            .replacePath(path)
            .replaceQuery(null)
            .fragment(null)
            .build()
            .toUriString();
    }

    private static String problemJson(String typeUri) {
        return """
            {
              "type": "%s",
              "title": "Authentication required",
              "status": 401,
              "detail": "A Bearer access token is required. Read the WWW-Authenticate header for OAuth metadata discovery, then go to API root (/) to learn this API's authentication flow."
            }
            """.formatted(
            escapeJson(typeUri));
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
