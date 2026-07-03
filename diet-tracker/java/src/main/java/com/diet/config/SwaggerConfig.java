package com.diet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Diet Tracker API")
                .version("1.0.0")
                .description("System kalkulatora zapotrzebowania kalorycznego"))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .name("basicAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                ));
    }
}
