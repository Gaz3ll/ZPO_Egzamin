package pl.zpo.app.exception;

import pl.zpo.app.common.ErrorCode;

/** HTTP 409 — the request conflicts with current state (e.g. duplicate email, time collision). */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
