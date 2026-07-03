package pl.zpo.app.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Uniform response envelope for every endpoint.
 *
 * <p>Success: {@code { "success": true,  "data": {...}, "error": null }}<br>
 * Failure: {@code { "success": false, "data": null,  "error": {...} }}</p>
 *
 * @param <T> payload type on success
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
        boolean success,
        T data,
        DomainError error
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(DomainError error) {
        return new ApiResponse<>(false, null, error);
    }
}
