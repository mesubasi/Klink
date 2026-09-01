package com.urlshortener.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "basicAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Klink URL Shortener API Dokümantasyonu")
                        .version("1.0.0")
                        .description("Spring Boot 3, Redis Cache, RabbitMQ ve Spring Security destekli yüksek performanslı URL kısaltıcı REST API servisleri.")
                        .contact(new Contact()
                                .name("Klink Destek")
                                .email("info@klink.local"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic Auth kimlik doğrulaması. Kullanıcılar: 'user' / 'password' veya 'admin' / 'admin123'")));
    }
}
