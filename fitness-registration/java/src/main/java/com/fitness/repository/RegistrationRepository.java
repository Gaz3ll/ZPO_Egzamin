package com.fitness.repository;

import com.fitness.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByClassIdAndStatus(Long classId, String status);
    Optional<Registration> findByUserIdAndClassId(String userId, Long classId);
    long countByClassIdAndStatus(Long classId, String status);
    List<Registration> findByClassIdAndStatusOrderByPosition(Long classId, String status);
}
