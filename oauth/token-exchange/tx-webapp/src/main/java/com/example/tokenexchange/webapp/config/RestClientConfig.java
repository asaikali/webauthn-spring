package com.example.tokenexchange.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient uppercaseRestClient(@Value("${tx.uppercase-base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }
}
