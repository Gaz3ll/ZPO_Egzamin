package pl.zpo.app.exception;

import pl.zpo.app.common.ErrorCode;

/** HTTP 403 — authenticated but not allowed to perform this action. */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
