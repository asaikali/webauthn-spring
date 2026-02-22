package com.example;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
public class QuoteController {

  private final WebClient webClient;

  public QuoteController(WebClient webClient) {
    this.webClient = webClient;
  }

  @GetMapping("/")
  public Quote randomQuote(@RegisteredOAuth2AuthorizedClient("demoAuthServer") OAuth2AuthorizedClient client) {

    System.out.println("using token value: " + client.getAccessToken().getTokenValue());
    Quote quote = webClient
            .get()
            .uri("http://127.0.0.1:8083/random-quote")
            .attributes(oauth2AuthorizedClient(client))
            .retrieve()
            .bodyToMono(Quote.class)
            .block();

    quote.setQuote(quote.getQuote().toUpperCase());

    return quote;
  }
}
