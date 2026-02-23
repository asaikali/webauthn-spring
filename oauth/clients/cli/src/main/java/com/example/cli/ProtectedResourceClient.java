package com.example.cli;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class ProtectedResourceClient {

  private final WebClient webClient;
  private final CliProperties properties;
  private final ObjectMapper objectMapper;

  public ProtectedResourceClient(WebClient.Builder webClientBuilder, CliProperties properties, ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.build();
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  public String readQuote(String accessToken) {
    String responseBody = this.webClient.get()
        .uri(this.properties.getResourceServerBaseUrl() + "/random-quote")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class)
        .block();
    return prettyJson(responseBody);
  }

  public String whoami(String accessToken) {
    String responseBody = this.webClient.get()
        .uri(this.properties.getResourceServerBaseUrl() + "/whoami")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class)
        .block();
    return prettyJson(responseBody);
  }

  private String prettyJson(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }
    try {
      Object parsed = this.objectMapper.readValue(input, Object.class);
      return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
    } catch (Exception ex) {
      return input;
    }
  }
}
