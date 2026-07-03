package pl.zpo.app.exception;

import java.util.Map;
import pl.zpo.app.common.ErrorCode;

/**
 * Base class for expected, client-facing errors. Each carries a machine-readable
 * {@link ErrorCode} (which maps to an HTTP status) and optional field-level messages.
 * {@code pl.zpo.app.exception.GlobalExceptionHandler} translates these into
 * {@code ApiResponse} error bodies.
 */
public abstract class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Map<String, String> fieldErrors;

    protected ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected ApiException(ErrorCode errorCode, String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
