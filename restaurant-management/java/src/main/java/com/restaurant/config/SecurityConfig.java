package com.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/logout", "/css/**", "/js/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/restaurant/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login.defaultSuccessUrl("/restaurant", true))
            .logout(logout -> logout.logoutSuccessUrl("/login"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var manager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = passwordEncoder();
        
        manager.createUser(User.withUsername("guest1").password(encoder.encode("pass")).roles("GUEST").build());
        manager.createUser(User.withUsername("guest2").password(encoder.encode("pass")).roles("GUEST").build());
        manager.createUser(User.withUsername("waiter1").password(encoder.encode("pass")).roles("WAITER").build());
        manager.createUser(User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build());
        return manager;
    }
}
