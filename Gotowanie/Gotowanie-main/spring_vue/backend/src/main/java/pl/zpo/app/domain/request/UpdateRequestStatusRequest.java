package pl.zpo.app.domain.request;

import jakarta.validation.constraints.NotNull;

/** Payload for ADMIN/OPERATOR status transitions on a request. */
public record UpdateRequestStatusRequest(
        @NotNull RequestStatus status
) {
}
