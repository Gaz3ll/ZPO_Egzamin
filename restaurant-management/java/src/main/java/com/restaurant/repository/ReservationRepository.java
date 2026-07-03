package com.restaurant.repository;

import com.restaurant.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByTime(String time);
    List<Reservation> findByUserId(String userId);
    Optional<Reservation> findByTableIdAndTime(Long tableId, String time);
    Optional<Reservation> findByUserIdAndTime(String userId, String time);
}
