package pl.zpo.app.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.DomainError;
import pl.zpo.app.common.ErrorCode;

/**
 * Translates exceptions thrown inside controllers/services into the uniform
 * {@link ApiResponse} error envelope with the correct HTTP status. Authentication (401)
 * and authorization (403) failures raised inside the Spring Security filter chain are
 * handled separately in {@code SecurityConfig} so the JSON shape stays identical.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** All expected, client-facing errors (400/401/403/404/409). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex) {
        DomainError error = DomainError.of(ex.getErrorCode(), ex.getMessage(), ex.getFieldErrors());
        return ResponseEntity.status(ex.getErrorCode().status()).body(ApiResponse.fail(error));
    }

    /** Bean Validation on @Valid @RequestBody → 400 with per-field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        DomainError error = DomainError.of(ErrorCode.VALIDATION_ERROR, "Validation failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(error));
    }

    /** Bean Validation on @Validated method params → 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleParamValidation(ConstraintViolationException ex) {
        DomainError error = DomainError.of(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(error));
    }

    /** Malformed / unreadable JSON body → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        DomainError error = DomainError.of(ErrorCode.VALIDATION_ERROR, "Malformed request body");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(error));
    }

    /** Anything unexpected → 500 (details are logged, not leaked to the client). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        DomainError error = DomainError.of(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(error));
    }
}
