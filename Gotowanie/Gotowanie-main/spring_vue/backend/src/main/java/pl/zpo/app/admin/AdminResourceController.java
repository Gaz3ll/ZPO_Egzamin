package pl.zpo.app.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.PageResponse;
import pl.zpo.app.domain.resource.CreateResourceRequest;
import pl.zpo.app.domain.resource.ResourceDto;
import pl.zpo.app.domain.resource.ResourceService;
import pl.zpo.app.domain.resource.UpdateResourceRequest;

/**
 * ADMIN-only resource management. Access is enforced server-side in {@code SecurityConfig}
 * ({@code /api/admin/resources/**} requires ROLE_ADMIN) — the UI merely reflects that.
 */
@RestController
@RequestMapping("/api/admin/resources")
@Tag(name = "Admin · Resources", description = "Create and update resources (ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminResourceController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "List all resources including inactive (paged)")
    public ApiResponse<PageResponse<ResourceDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE), Sort.by("id").descending());
        Page<ResourceDto> result = resourceService.listAll(pageable);
        return ApiResponse.ok(PageResponse.from(result, result.getContent()));
    }

    @PostMapping
    @Operation(summary = "Create a resource")
    public ApiResponse<ResourceDto> create(@Valid @RequestBody CreateResourceRequest request) {
        return ApiResponse.ok(resourceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resource")
    public ApiResponse<ResourceDto> update(@PathVariable Long id,
                                           @Valid @RequestBody UpdateResourceRequest request) {
        return ApiResponse.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ApiResponse.ok(null);
    }
}
