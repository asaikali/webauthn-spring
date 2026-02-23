package com.example.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CliApplication {

  public static void main(String[] args) {
    SpringApplication.run(CliApplication.class, args);
  }
}
