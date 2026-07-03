package pl.zpo.app.domain.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zpo.app.common.PageResponse;
import pl.zpo.app.domain.algorithm.DomainAlgorithm;
import pl.zpo.app.domain.algorithm.DomainAlgorithmInput;
import pl.zpo.app.domain.algorithm.DomainAlgorithmResult;
import pl.zpo.app.domain.config.DomainFieldValidator;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.policy.RequestAccessPolicy;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;
import pl.zpo.app.domain.resource.ResourceStatus;
import pl.zpo.app.exception.ConflictException;
import pl.zpo.app.exception.ForbiddenException;
import pl.zpo.app.exception.NotFoundException;
import pl.zpo.app.exception.ValidationException;
import pl.zpo.app.security.CurrentUser;
import pl.zpo.app.security.CurrentUserService;
import pl.zpo.app.users.Role;
import pl.zpo.app.users.UserEntity;
import pl.zpo.app.users.UserRepository;

/**
 * Orchestrates the request lifecycle. The heavy domain logic lives in the algorithm and policies;
 * this service wires them to persistence and translates outcomes into the right HTTP semantics
 * (400 invalid input, 403 forbidden, 404 missing, 409 availability conflict).
 */
@Service
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    private final DomainAlgorithm algorithm;
    private final DomainFieldValidator fieldValidator;
    private final RequestAccessPolicy accessPolicy;
    private final CurrentUserService currentUserService;
    private final DomainProfile profile;

    public RequestService(RequestRepository requestRepository,
                          ResourceRepository resourceRepository,
                          UserRepository userRepository,
                          RequestMapper requestMapper,
                          DomainAlgorithm algorithm,
                          DomainFieldValidator fieldValidator,
                          RequestAccessPolicy accessPolicy,
                          CurrentUserService currentUserService,
                          DomainProfile profile) {
        this.requestRepository = requestRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.requestMapper = requestMapper;
        this.algorithm = algorithm;
        this.fieldValidator = fieldValidator;
        this.accessPolicy = accessPolicy;
        this.currentUserService = currentUserService;
        this.profile = profile;
    }

    /** Create a request: validate input, run the algorithm, persist the result. */
    public RequestDto create(CreateDomainRequest request) {
        CurrentUser user = currentUserService.requireUser();
        ResourceEntity resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> NotFoundException.of("Resource", request.resourceId()));

        // Domain metadata validation (400) driven by the active profile.
        fieldValidator.validate(request.metadata(), profile.requestFields(), profile.requestLabelSingular());

        // Fixed-field validation with precise HTTP semantics.
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            throw new ConflictException("Zasób nie jest aktywny i nie można go zarezerwować");
        }
        boolean hasWindow = request.startAt() != null && request.endAt() != null;
        if (profile.requiresTimeWindow() && !hasWindow) {
            throw new ValidationException("Wymagany jest zakres dat (startAt, endAt)");
        }
        if (request.startAt() != null && request.endAt() != null
                && !request.startAt().isBefore(request.endAt())) {
            throw new ValidationException("Nieprawidłowy zakres dat: początek musi być przed końcem");
        }
        if (profile.requiresQuantity() && request.quantity() == null) {
            throw new ValidationException("Wymagana jest ilość (quantity)");
        }

        // Run the (pure) domain algorithm against the current active bookings.
        List<RequestEntity> existing = requestRepository.findAllByResourceIdAndStatusIn(
                resource.getId(), RequestStatus.activeStatuses());
        DomainAlgorithmResult result = algorithm.evaluate(new DomainAlgorithmInput(
                resource, request.startAt(), request.endAt(), request.quantity(),
                request.metadata(), existing, profile));

        if (!result.success()) {
            throw new ConflictException(String.join("; ", result.errors()));
        }

        Long assignedResourceId = result.assignedResourceId() != null
                ? result.assignedResourceId()
                : resource.getId();
        ResourceEntity assignedResource = Objects.equals(assignedResourceId, resource.getId())
                ? resource
                : resourceRepository.findById(assignedResourceId)
                        .orElseThrow(() -> NotFoundException.of("Resource", assignedResourceId));

        RequestEntity entity = new RequestEntity();
        entity.setOwnerId(user.id());
        entity.setResourceId(assignedResource.getId());
        entity.setStatus(RequestStatus.PENDING);
        entity.setStartAt(request.startAt());
        entity.setEndAt(request.endAt());
        entity.setQuantity(request.quantity());
        entity.setMetadata(request.metadata() != null ? new HashMap<>(request.metadata()) : new HashMap<>());
        entity.setCalculatedValue(result.calculatedValue());
        entity.setAlgorithmBreakdown(result.breakdown());
        RequestEntity saved = requestRepository.save(entity);

        return requestMapper.toDto(saved, assignedResource.getName(), user.email(), profile.currency());
    }

    @Transactional(readOnly = true)
    public RequestDto getById(Long id) {
        CurrentUser user = currentUserService.requireUser();
        RequestEntity entity = findOr404(id);
        if (!accessPolicy.canRead(user, entity)) {
            throw new ForbiddenException("Nie masz dostępu do tego zgłoszenia");
        }
        return mapSingle(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<RequestDto> listMine(Pageable pageable) {
        CurrentUser user = currentUserService.requireUser();
        Page<RequestEntity> page = requestRepository.findAllByOwnerIdOrderByCreatedAtDesc(user.id(), pageable);
        return PageResponse.from(page, mapAll(page.getContent()));
    }

    @Transactional(readOnly = true)
    public PageResponse<RequestDto> listAll(RequestStatus statusFilter, Pageable pageable) {
        Page<RequestEntity> page = statusFilter == null
                ? requestRepository.findAllByOrderByCreatedAtDesc(pageable)
                : requestRepository.findAllByStatusOrderByCreatedAtDesc(statusFilter, pageable);
        return PageResponse.from(page, mapAll(page.getContent()));
    }

    /** Active requests for a given resource (for availability checks). */
    @Transactional(readOnly = true)
    public List<RequestDto> listByResource(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw NotFoundException.of("Resource", resourceId);
        }
        List<RequestEntity> entities = requestRepository.findAllByResourceIdAndStatusIn(
                resourceId, RequestStatus.activeStatuses());
        return mapAll(entities);
    }

    /** Owner (or ADMIN) cancels a cancellable request. */
    public RequestDto cancel(Long id) {
        CurrentUser user = currentUserService.requireUser();
        RequestEntity entity = findOr404(id);
        if (!accessPolicy.canCancel(user, entity)) {
            boolean ownerOrAdmin = user.isAdmin() || Objects.equals(user.id(), entity.getOwnerId());
            if (!ownerOrAdmin) {
                throw new ForbiddenException("Nie możesz anulować tego zgłoszenia");
            }
            throw new ConflictException("Nie można anulować zgłoszenia w statusie " + entity.getStatus());
        }
        entity.setStatus(RequestStatus.CANCELLED);
        RequestDto result = mapSingle(requestRepository.save(entity));
        promoteWaitlisted(entity.getResourceId());
        return result;
    }

    /** Owner returns/returns a request (sets to COMPLETED). */
    public RequestDto returnRequest(Long id) {
        CurrentUser user = currentUserService.requireUser();
        RequestEntity entity = findOr404(id);
        if (!Objects.equals(user.id(), entity.getOwnerId()) && !user.isAdmin()) {
            throw new ForbiddenException("Nie możesz zwrócić tego zgłoszenia");
        }
        if (entity.getStatus() == RequestStatus.CANCELLED || entity.getStatus() == RequestStatus.COMPLETED) {
            throw new ConflictException("Nie można zwrócić zgłoszenia w statusie " + entity.getStatus());
        }
        entity.setStatus(RequestStatus.COMPLETED);
        return mapSingle(requestRepository.save(entity));
    }

    /** ADMIN/OPERATOR changes a request's status. */
    public RequestDto updateStatus(Long id, RequestStatus status) {
        CurrentUser user = currentUserService.requireAnyRole(Role.ADMIN, Role.OPERATOR);
        RequestEntity entity = findOr404(id);
        if (!accessPolicy.canManage(user, entity)) {
            throw new ForbiddenException("Nie możesz zarządzać tym zgłoszeniem");
        }
        RequestStatus previousStatus = entity.getStatus();
        entity.setStatus(status);
        RequestDto result = mapSingle(requestRepository.save(entity));

        // Waitlist promotion: if someone cancels/returns, promote first waitlisted
        if ((status == RequestStatus.CANCELLED || status == RequestStatus.COMPLETED)
                && previousStatus != RequestStatus.CANCELLED
                && previousStatus != RequestStatus.COMPLETED) {
            promoteWaitlisted(entity.getResourceId());
        }

        return result;
    }

    private void promoteWaitlisted(Long resourceId) {
        List<RequestEntity> waitlisted = requestRepository
                .findAllByResourceIdAndStatusIn(resourceId, List.of(RequestStatus.PENDING));
        if (waitlisted.isEmpty()) return;

        // Promote the oldest waitlisted request
        RequestEntity toPromote = waitlisted.stream()
                .min(java.util.Comparator.comparing(RequestEntity::getCreatedAt))
                .orElse(null);
        if (toPromote != null) {
            toPromote.setStatus(RequestStatus.CONFIRMED);
            requestRepository.save(toPromote);
        }
    }

    /** ADMIN/OPERATOR permanently deletes a request. */
    public void delete(Long id) {
        CurrentUser user = currentUserService.requireAnyRole(Role.ADMIN, Role.OPERATOR);
        RequestEntity entity = findOr404(id);
        if (!accessPolicy.canManage(user, entity)) {
            throw new ForbiddenException("Nie możesz usunąć tego zgłoszenia");
        }
        requestRepository.delete(entity);
    }

    private RequestEntity findOr404(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Request", id));
    }

    private RequestDto mapSingle(RequestEntity entity) {
        String resourceName = resourceRepository.findById(entity.getResourceId())
                .map(ResourceEntity::getName).orElse(null);
        String ownerEmail = userRepository.findById(entity.getOwnerId())
                .map(UserEntity::getEmail).orElse(null);
        return requestMapper.toDto(entity, resourceName, ownerEmail, profile.currency());
    }

    /** Batch-resolve resource names and owner emails to avoid N+1 lookups when listing. */
    private List<RequestDto> mapAll(List<RequestEntity> entities) {
        Set<Long> resourceIds = entities.stream().map(RequestEntity::getResourceId).collect(Collectors.toSet());
        Set<Long> ownerIds = entities.stream().map(RequestEntity::getOwnerId).collect(Collectors.toSet());

        Map<Long, String> resourceNames = resourceRepository.findAllById(resourceIds).stream()
                .collect(Collectors.toMap(ResourceEntity::getId, ResourceEntity::getName));
        Map<Long, String> ownerEmails = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getEmail));

        return entities.stream()
                .map(e -> requestMapper.toDto(e,
                        resourceNames.get(e.getResourceId()),
                        ownerEmails.get(e.getOwnerId()),
                        profile.currency()))
                .toList();
    }
}
