package com.example.quotes;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
public class QuotesController {
  private final WebClient webClient;

  public QuotesController(WebClient webClient) {
    this.webClient = webClient;
  }

  @GetMapping("/random-quote")
  public Quote randomQuote(
          @RegisteredOAuth2AuthorizedClient("quotes-reader")
          OAuth2AuthorizedClient authorizedClient) {

    Quote result = this.webClient
            .get()
            .uri("http://localhost:8081/random-quote")
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(Quote.class)
            .block();
    return result;
  }

}