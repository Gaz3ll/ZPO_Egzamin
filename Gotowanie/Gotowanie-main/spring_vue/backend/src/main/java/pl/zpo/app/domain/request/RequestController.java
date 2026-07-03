package pl.zpo.app.domain.request;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.PageResponse;
import java.util.List;

/**
 * Authenticated request endpoints for regular users. A user only ever sees or cancels their own
 * requests — enforced server-side in {@code RequestService} via {@code RequestAccessPolicy}.
 */
@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Create, view and cancel your own requests")
@SecurityRequirement(name = "bearerAuth")
public class RequestController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @Operation(summary = "Create a request; the backend runs the domain algorithm and returns the breakdown")
    public ApiResponse<RequestDto> create(@Valid @RequestBody CreateDomainRequest request) {
        return ApiResponse.ok(requestService.create(request));
    }

    @GetMapping("/my")
    @Operation(summary = "List the current user's requests (paged)")
    public ApiResponse<PageResponse<RequestDto>> myRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), clampSize(size));
        return ApiResponse.ok(requestService.listMine(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of the current user's requests by id")
    public ApiResponse<RequestDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(requestService.getById(id));
    }

    @GetMapping("/by-resource/{resourceId}")
    @Operation(summary = "Active requests for a resource (for availability checks)")
    public ApiResponse<List<RequestDto>> byResource(@PathVariable Long resourceId) {
        return ApiResponse.ok(requestService.listByResource(resourceId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel one of the current user's requests")
    public ApiResponse<RequestDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(requestService.cancel(id));
    }

    @PatchMapping("/{id}/return")
    @Operation(summary = "Return/complete one of the current user's requests")
    public ApiResponse<RequestDto> returnRequest(@PathVariable Long id) {
        return ApiResponse.ok(requestService.returnRequest(id));
    }

    private int clampSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
