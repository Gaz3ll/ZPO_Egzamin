package pl.zpo.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.zpo.app.users.Role;
import pl.zpo.app.users.UserEntity;

/**
 * Issues and verifies stateless JWT access tokens (HMAC-SHA256).
 *
 * <p>We chose JWT over server-side sessions because the frontend is a separate-origin SPA:
 * stateless tokens avoid a shared session store, scale horizontally, and keep the API purely
 * REST. The token carries the user id (subject), email and role, so authorization needs no
 * database lookup per request.</p>
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms}") long expirationMs,
            @Value("${app.security.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    /** Issue a signed token for the given user. */
    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    /** Lifetime of issued tokens, in seconds (returned to the client after login). */
    public long expiresInSeconds() {
        return expirationMs / 1000;
    }

    /**
     * Verify signature, issuer and expiry, then extract the principal.
     * Returns empty for any invalid/expired/tampered token (never throws to the caller).
     */
    public Optional<TokenData> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            Role role = Role.valueOf(claims.get("role", String.class));
            return Optional.of(new TokenData(userId, email, role));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /** Decoded, verified token contents. */
    public record TokenData(Long userId, String email, Role role) {
    }
}
