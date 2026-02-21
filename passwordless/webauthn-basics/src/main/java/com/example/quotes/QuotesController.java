package com.example.quotes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuotesController {

  private final QuoteRepository quoteRepository;

  public QuotesController(QuoteRepository quoteRepository) {
    this.quoteRepository = quoteRepository;
  }

  @GetMapping("/random-quote")
  public Quote randomQuote() {
    Quote result = quoteRepository.findRandomQuote();
    return result;
  }
}
