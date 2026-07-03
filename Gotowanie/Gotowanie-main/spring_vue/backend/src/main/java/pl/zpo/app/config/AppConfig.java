package pl.zpo.app.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General application beans. A {@link Clock} is exposed so time-dependent services
 * (availability / collision checks) can be tested deterministically.
 */
@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
