package pl.zpo.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.DomainError;
import pl.zpo.app.common.ErrorCode;

/**
 * Central security configuration.
 *
 * <ul>
 *   <li>Stateless (JWT) — no HTTP session; every request re-authenticates from the token.</li>
 *   <li>Authorization is enforced on the server here (not by hiding buttons in the SPA):
 *       admin resource management requires ADMIN; request handling requires ADMIN or OPERATOR;
 *       everything else under {@code /api/**} requires authentication.</li>
 *   <li>401/403 are returned as the same {@link ApiResponse} JSON envelope as other errors.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/config", "/api/config/**").permitAll()
                        // OpenAPI / Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Admin area — resource management is ADMIN only
                        .requestMatchers("/api/admin/resources/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/dental/dentists/**").hasRole("ADMIN")
                        // Request handling may also be done by OPERATOR
                        .requestMatchers("/api/admin/requests/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/admin/dental/appointments/**").hasAnyRole("ADMIN", "OPERATOR")
                        // Everything else requires an authenticated user
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 401 for unauthenticated access to a protected endpoint. */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                writeError(response, ErrorCode.UNAUTHORIZED, "Authentication is required");
    }

    /** 403 for authenticated users lacking the required role. */
    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) ->
                writeError(response, ErrorCode.FORBIDDEN, "You do not have permission to access this resource");
    }

    private void writeError(HttpServletResponse response, ErrorCode code, String message) throws java.io.IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.fail(DomainError.of(code, message));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
