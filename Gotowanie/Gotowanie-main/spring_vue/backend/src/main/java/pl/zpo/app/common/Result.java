package pl.zpo.app.common;

import java.util.Optional;
import java.util.function.Function;

/**
 * Lightweight functional result used inside the domain (e.g. the algorithm's validation
 * pipeline) to represent success-or-failure without throwing exceptions.
 *
 * @param <T> value type on success
 */
public record Result<T>(boolean success, T value, String error) {

    public static <T> Result<T> ok(T value) {
        return new Result<>(true, value, null);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public Optional<T> toOptional() {
        return success ? Optional.ofNullable(value) : Optional.empty();
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        return success ? Result.ok(mapper.apply(value)) : Result.fail(error);
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return success ? mapper.apply(value) : Result.fail(error);
    }
}
