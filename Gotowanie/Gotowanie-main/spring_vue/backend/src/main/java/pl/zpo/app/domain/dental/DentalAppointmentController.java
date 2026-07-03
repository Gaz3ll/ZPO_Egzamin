package pl.zpo.app.domain.dental;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.common.PageResponse;
import pl.zpo.app.domain.request.CreateDomainRequest;
import pl.zpo.app.domain.request.RequestDto;
import pl.zpo.app.domain.request.RequestService;
import pl.zpo.app.domain.resource.ResourceDto;
import pl.zpo.app.domain.resource.ResourceService;

@RestController
@RequestMapping("/api/dental-appointments")
@Tag(name = "Dental Appointments", description = "Book and manage your dental appointments")
@SecurityRequirement(name = "bearerAuth")
public class DentalAppointmentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RequestService requestService;
    private final ResourceService resourceService;

    public DentalAppointmentController(RequestService requestService, ResourceService resourceService) {
        this.requestService = requestService;
        this.resourceService = resourceService;
    }

    @PostMapping
    @Operation(summary = "Book a dental appointment; the backend runs the pricing algorithm and returns a breakdown")
    public ApiResponse<RequestDto> book(@Valid @RequestBody CreateDomainRequest request) {
        return ApiResponse.ok(requestService.create(request));
    }

    @GetMapping("/my")
    @Operation(summary = "List the current patient's appointments (paged)")
    public ApiResponse<PageResponse<RequestDto>> myAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), clampSize(size));
        return ApiResponse.ok(requestService.listMine(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of the current patient's appointments by id")
    public ApiResponse<RequestDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(requestService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel one of the current patient's appointments")
    public ApiResponse<RequestDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok(requestService.cancel(id));
    }

    @GetMapping("/dentists")
    @Operation(summary = "List all available dentists (paged)")
    public ApiResponse<PageResponse<ResourceDto>> listDentists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), clampSize(size), Sort.by("name").ascending());
        Page<ResourceDto> result = resourceService.listActive(pageable);
        return ApiResponse.ok(PageResponse.from(result, result.getContent()));
    }

    private int clampSize(int size) {
        if (size < 1) return 20;
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
