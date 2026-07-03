package pl.zpo.app.domain.resource;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

    Page<ResourceEntity> findAllByStatus(ResourceStatus status, Pageable pageable);

    List<ResourceEntity> findAllByStatusOrderByNameAsc(ResourceStatus status);

    boolean existsByIdAndStatus(Long id, ResourceStatus status);
}
