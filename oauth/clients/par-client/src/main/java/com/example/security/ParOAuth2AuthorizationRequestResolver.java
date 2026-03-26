package com.example.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class ParOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final OAuth2AuthorizationRequestResolver delegate;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final RestClient restClient;

  public ParOAuth2AuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.delegate =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.restClient = RestClient.create();
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    String registrationId = resolveRegistrationId(request);
    if (registrationId == null) {
      return null;
    }
    return resolve(request, registrationId);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
    if (authorizationRequest == null) {
      return null;
    }

    ClientRegistration clientRegistration =
        clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    ParResponse parResponse = pushAuthorizationRequest(clientRegistration, authorizationRequest);

    String authorizationUri =
        clientRegistration.getProviderDetails().getAuthorizationUri()
            + "?client_id="
            + UriUtils.encodeQueryParam(clientRegistration.getClientId(), java.nio.charset.StandardCharsets.UTF_8)
            + "&request_uri="
            + UriUtils.encodeQueryParam(parResponse.requestUri(), java.nio.charset.StandardCharsets.UTF_8);

    return OAuth2AuthorizationRequest.from(authorizationRequest)
        .authorizationRequestUri(authorizationUri)
        .build();
  }

  private String resolveRegistrationId(HttpServletRequest request) {
    String prefix =
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/";
    String requestUri = request.getRequestURI();
    if (!requestUri.startsWith(prefix) || requestUri.length() <= prefix.length()) {
      return null;
    }
    return requestUri.substring(prefix.length());
  }

  private ParResponse pushAuthorizationRequest(
      ClientRegistration clientRegistration, OAuth2AuthorizationRequest authorizationRequest) {
    var uriComponents =
        UriComponentsBuilder.fromUriString(authorizationRequest.getAuthorizationRequestUri())
            .build(true);
    var formParameters = new LinkedMultiValueMap<String, String>();
    for (Map.Entry<String, List<String>> entry : uriComponents.getQueryParams().entrySet()) {
      formParameters.put(
          entry.getKey(),
          entry.getValue().stream()
              .map(value -> UriUtils.decode(value, java.nio.charset.StandardCharsets.UTF_8))
              .toList());
    }

    return restClient
        .post()
        .uri(toParEndpoint(clientRegistration.getProviderDetails().getAuthorizationUri()))
        .headers(
            headers -> {
              if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(
                  clientRegistration.getClientAuthenticationMethod())) {
                headers.setBasicAuth(
                    clientRegistration.getClientId(), clientRegistration.getClientSecret());
              }
            })
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .body(formParameters)
        .retrieve()
        .body(ParResponse.class);
  }

  private String toParEndpoint(String authorizationEndpoint) {
    var components = UriComponentsBuilder.fromUriString(authorizationEndpoint).build(true);
    String path = components.getPath();
    if (path == null || !path.endsWith("/oauth2/authorize")) {
      throw new IllegalStateException(
          "Expected authorization endpoint path to end with /oauth2/authorize but got "
              + authorizationEndpoint);
    }
    return UriComponentsBuilder.fromUriString(authorizationEndpoint)
        .replacePath(path.replace("/oauth2/authorize", "/oauth2/par"))
        .replaceQuery(null)
        .build(true)
        .toUriString();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record ParResponse(String request_uri) {
    String requestUri() {
      return request_uri;
    }
  }
}
