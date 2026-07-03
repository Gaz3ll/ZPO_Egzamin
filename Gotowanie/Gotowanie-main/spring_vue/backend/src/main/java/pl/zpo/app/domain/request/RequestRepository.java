package pl.zpo.app.domain.request;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    /** A single user's requests (most recent first). */
    Page<RequestEntity> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    /** All requests, optionally filtered by status (admin/operator view). */
    Page<RequestEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<RequestEntity> findAllByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    /**
     * Active requests for a resource — the input the algorithm needs to detect time
     * collisions and used capacity. Excludes the request being edited (if any).
     */
    List<RequestEntity> findAllByResourceIdAndStatusIn(Long resourceId, Collection<RequestStatus> statuses);
}
