package pl.zpo.app.domain.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.PageResponse;

/**
 * Read-only, authenticated resource endpoints for regular users. Management lives in
 * {@code AdminResourceController}.
 */
@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resources", description = "Browse resources and check availability")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "List active resources (paged)")
    public ApiResponse<PageResponse<ResourceDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), clampSize(size), Sort.by("name").ascending());
        Page<ResourceDto> result = resourceService.listActive(pageable);
        return ApiResponse.ok(PageResponse.from(result, result.getContent()));
    }

    @GetMapping("/available")
    @Operation(summary = "List active resources available for an optional time window and quantity")
    public ApiResponse<List<ResourceDto>> available(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(required = false) Integer quantity) {
        return ApiResponse.ok(resourceService.findAvailable(start, end, quantity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single resource by id")
    public ApiResponse<ResourceDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(resourceService.getById(id));
    }

    private int clampSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
