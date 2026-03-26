package com.example.moneymate.protectedtestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableHypermediaSupport(type = { HypermediaType.HAL, HypermediaType.HAL_FORMS })
public class ProtectedTestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProtectedTestApiApplication.class, args);
    }
}
