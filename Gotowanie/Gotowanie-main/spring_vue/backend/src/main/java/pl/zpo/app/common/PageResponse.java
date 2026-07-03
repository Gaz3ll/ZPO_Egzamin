package pl.zpo.app.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Serialization-friendly page wrapper. We do not expose Spring Data's {@code Page}
 * directly because its JSON shape is unstable across versions.
 *
 * @param <T> element type
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** Build a page from already-mapped content plus the source {@link Page}. */
    public static <T, S> PageResponse<T> from(Page<S> source, List<T> mappedItems) {
        return new PageResponse<>(
                mappedItems,
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages()
        );
    }
}
