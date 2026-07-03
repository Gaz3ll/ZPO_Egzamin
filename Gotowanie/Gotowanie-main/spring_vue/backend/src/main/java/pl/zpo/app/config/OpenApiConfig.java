package pl.zpo.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI definition. Declares a global {@code bearerAuth} scheme so the
 * "Authorize" button in Swagger UI lets you paste a JWT and call protected endpoints.
 * UI available at {@code /swagger-ui/index.html}.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI appOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("System obsługi warsztatu samochodowego API")
                        .version("1.0.0")
                        .description("""
                                API do obsługi stanowisk serwisowych i zleceń warsztatowych.
                                Backend sprawdza dostępność stanowiska, kolizje terminów,
                                dopasowanie usługi oraz wylicza koszt z breakdownem algorytmu.
                                Zaloguj się przez POST /api/auth/login, potem użyj Authorize.""")
                        .contact(new Contact().name("Warsztat Samochodowy"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
