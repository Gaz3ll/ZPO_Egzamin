package pl.zpo.app.common;

import java.time.Instant;
import java.util.Map;

/**
 * Structured error payload embedded in {@link ApiResponse}. Field-level validation
 * problems are reported through {@link #fieldErrors()} (field name -&gt; message).
 */
public record DomainError(
        String code,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {

    public static DomainError of(ErrorCode code, String message) {
        return new DomainError(code.name(), message, null, Instant.now());
    }

    public static DomainError of(ErrorCode code, String message, Map<String, String> fieldErrors) {
        return new DomainError(code.name(), message, fieldErrors, Instant.now());
    }
}
