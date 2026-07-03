package pl.zpo.app.exception;

import pl.zpo.app.common.ErrorCode;

/** HTTP 401 — the caller is not authenticated (missing/invalid credentials or token). */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
