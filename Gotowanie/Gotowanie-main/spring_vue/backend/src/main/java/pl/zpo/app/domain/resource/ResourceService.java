package pl.zpo.app.domain.resource;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zpo.app.domain.availability.AvailabilityService;
import pl.zpo.app.domain.config.DomainFieldValidator;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.exception.NotFoundException;

/**
 * Business logic for resources: paged listings, availability filtering, and ADMIN create/update.
 * Domain metadata is validated against the active {@link DomainProfile} before persistence.
 */
@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final DomainFieldValidator fieldValidator;
    private final AvailabilityService availabilityService;
    private final DomainProfile profile;

    public ResourceService(ResourceRepository resourceRepository,
                           ResourceMapper resourceMapper,
                           DomainFieldValidator fieldValidator,
                           AvailabilityService availabilityService,
                           DomainProfile profile) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
        this.fieldValidator = fieldValidator;
        this.availabilityService = availabilityService;
        this.profile = profile;
    }

    @Transactional(readOnly = true)
    public Page<ResourceDto> listActive(Pageable pageable) {
        return resourceRepository.findAllByStatus(ResourceStatus.ACTIVE, pageable).map(resourceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ResourceDto> listAll(Pageable pageable) {
        return resourceRepository.findAll(pageable).map(resourceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ResourceDto getById(Long id) {
        return resourceMapper.toDto(getEntity(id));
    }

    /** Entity accessor for internal callers (e.g. the request flow needs the full resource). */
    @Transactional(readOnly = true)
    public ResourceEntity getEntity(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Resource", id));
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> findAvailable(Instant start, Instant end, Integer quantity) {
        return availabilityService.findAvailable(start, end, quantity).stream()
                .map(resourceMapper::toDto)
                .toList();
    }

    public ResourceDto create(CreateResourceRequest request) {
        fieldValidator.validate(request.metadata(), profile.resourceFields(), profile.resourceLabelSingular());
        ResourceEntity entity = resourceMapper.fromCreate(request);
        return resourceMapper.toDto(resourceRepository.save(entity));
    }

    public ResourceDto update(Long id, UpdateResourceRequest request) {
        fieldValidator.validate(request.metadata(), profile.resourceFields(), profile.resourceLabelSingular());
        ResourceEntity entity = getEntity(id);
        resourceMapper.applyUpdate(entity, request);
        return resourceMapper.toDto(resourceRepository.save(entity));
    }

    public void delete(Long id) {
        ResourceEntity entity = getEntity(id);
        resourceRepository.delete(entity);
    }
}
