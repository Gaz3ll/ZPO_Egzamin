package pl.zpo.app.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the active {@link DomainProfile} as a singleton bean so any service can inject it
 * directly. The definition itself lives in {@link DomainProfileProvider}.
 */
@Configuration
public class DomainConfig {

    @Bean
    public DomainProfile domainProfile(DomainProfileProvider provider) {
        return provider.getActiveProfile();
    }
}
