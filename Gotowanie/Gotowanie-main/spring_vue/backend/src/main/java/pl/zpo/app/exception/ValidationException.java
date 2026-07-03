package pl.zpo.app.exception;

import java.util.Map;
import pl.zpo.app.common.ErrorCode;

/** HTTP 400 — invalid input. May carry per-field messages (e.g. domain metadata validation). */
public class ValidationException extends ApiException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR, message, fieldErrors);
    }
}
