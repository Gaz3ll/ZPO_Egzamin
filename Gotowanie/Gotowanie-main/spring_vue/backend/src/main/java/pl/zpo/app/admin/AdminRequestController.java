package pl.zpo.app.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.PageResponse;
import pl.zpo.app.domain.request.RequestDto;
import pl.zpo.app.domain.request.RequestService;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.request.UpdateRequestStatusRequest;

/**
 * ADMIN/OPERATOR request handling. Access is enforced server-side in {@code SecurityConfig}
 * ({@code /api/admin/requests/**} requires ROLE_ADMIN or ROLE_OPERATOR).
 */
@RestController
@RequestMapping("/api/admin/requests")
@Tag(name = "Admin · Requests", description = "View all requests and change their status (ADMIN/OPERATOR)")
@SecurityRequirement(name = "bearerAuth")
public class AdminRequestController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RequestService requestService;

    public AdminRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @Operation(summary = "List all requests, optionally filtered by status (paged)")
    public ApiResponse<PageResponse<RequestDto>> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        return ApiResponse.ok(requestService.listAll(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change a request's status")
    public ApiResponse<RequestDto> updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRequestStatusRequest request) {
        return ApiResponse.ok(requestService.updateStatus(id, request.status()));
    }
}
