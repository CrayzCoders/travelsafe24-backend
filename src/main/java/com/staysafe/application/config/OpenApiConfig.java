package com.staysafe.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TravelSafe24 API")
                        .description("Find and compare Hamburg districts based on user lifestyle preferences. " +
                                "Computes weighted matching scores using amenity density, nightlife, and centrality criteria.")
                        .version("0.0.1"));
    }
}