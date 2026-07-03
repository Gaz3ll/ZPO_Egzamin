package pl.zpo.app.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around the configured {@link PasswordEncoder} (BCrypt). Centralizes hashing
 * and verification so no other class touches raw password encoding.
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** Hash a raw password for storage. */
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /** Verify a raw password against a stored BCrypt hash. */
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
