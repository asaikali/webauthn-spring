package com.example.quotes;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class QuotesController {

  private final QuoteRepository quoteRepository;
  private final Environment environment;

  public QuotesController(QuoteRepository quoteRepository, Environment environment) {
    this.quoteRepository = quoteRepository;
    this.environment = environment;
  }

  @ResponseBody
  @GetMapping("/random-quote")
  public Quote randomQuote() {
    Quote result = quoteRepository.findRandomQuote();
    return result;
  }

  @GetMapping("/quotes")
  public String quotes(){
    return "quotes";
  }
}
