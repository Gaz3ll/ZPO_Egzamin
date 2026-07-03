package pl.zpo.app.domain.dental;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import pl.zpo.app.domain.resource.CreateResourceRequest;
import pl.zpo.app.domain.resource.ResourceDto;
import pl.zpo.app.domain.resource.ResourceService;
import pl.zpo.app.domain.resource.UpdateResourceRequest;

@RestController
@RequestMapping("/api/admin/dental")
@Tag(name = "Admin · Dental Clinic", description = "Manage dentists and appointments (ADMIN/OPERATOR)")
@SecurityRequirement(name = "bearerAuth")
public class AdminDentalController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RequestService requestService;
    private final ResourceService resourceService;

    public AdminDentalController(RequestService requestService, ResourceService resourceService) {
        this.requestService = requestService;
        this.resourceService = resourceService;
    }

    @GetMapping("/dentists")
    @Operation(summary = "List all dentists including inactive (ADMIN)")
    public ApiResponse<PageResponse<ResourceDto>> listDentists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE), Sort.by("id").descending());
        var result = resourceService.listAll(pageable);
        return ApiResponse.ok(PageResponse.from(result, result.getContent()));
    }

    @PostMapping("/dentists")
    @Operation(summary = "Create a new dentist (ADMIN)")
    public ApiResponse<ResourceDto> createDentist(@Valid @RequestBody CreateResourceRequest request) {
        return ApiResponse.ok(resourceService.create(request));
    }

    @PutMapping("/dentists/{id}")
    @Operation(summary = "Update a dentist's details (ADMIN)")
    public ApiResponse<ResourceDto> updateDentist(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateResourceRequest request) {
        return ApiResponse.ok(resourceService.update(id, request));
    }

    @DeleteMapping("/dentists/{id}")
    @Operation(summary = "Delete a dentist (ADMIN)")
    public ApiResponse<Void> deleteDentist(@PathVariable Long id) {
        resourceService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/appointments")
    @Operation(summary = "List all appointments, optionally filtered by status (ADMIN/OPERATOR)")
    public ApiResponse<PageResponse<RequestDto>> listAppointments(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        return ApiResponse.ok(requestService.listAll(status, pageable));
    }

    @PatchMapping("/appointments/{id}/status")
    @Operation(summary = "Change an appointment's status (ADMIN/OPERATOR)")
    public ApiResponse<RequestDto> updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRequestStatusRequest request) {
        return ApiResponse.ok(requestService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/appointments/{id}")
    @Operation(summary = "Delete an appointment (ADMIN/OPERATOR)")
    public ApiResponse<Void> deleteAppointment(@PathVariable Long id) {
        requestService.delete(id);
        return ApiResponse.ok(null);
    }
}
