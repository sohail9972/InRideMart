package com.inridemart.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("InRideMart Auth Service API")
                .version("0.1.0")
                .description("Authentication and token APIs for InRideMart."));
    }
}

