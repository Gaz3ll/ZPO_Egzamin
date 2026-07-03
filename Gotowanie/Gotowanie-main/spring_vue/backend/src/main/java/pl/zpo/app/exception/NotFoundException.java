package pl.zpo.app.exception;

import pl.zpo.app.common.ErrorCode;

/** HTTP 404 — the requested entity does not exist. */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException("%s with id %s was not found".formatted(entity, id));
    }
}
