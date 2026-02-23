package com.example.cli;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.shell.interactive.enabled=false")
class CliApplicationTests {

  @Test
  void contextLoads() {}
}
