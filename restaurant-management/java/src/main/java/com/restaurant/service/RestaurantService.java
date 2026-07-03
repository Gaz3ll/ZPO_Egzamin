package com.restaurant.service;

import com.restaurant.model.RestaurantTable;
import com.restaurant.model.Reservation;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Serwis zarządzający rezerwacjami stolików w restauracji.
 * Wszystkie algorytmy wyszukiwania i rezerwacji zostały napisane funkcyjnie (bez pętli oraz ifów).
 */
@Service
public class RestaurantService {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public RestaurantService(TableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByUser(String userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Zwraca optymalny wolny stolik spełniający warunki (podejście funkcyjne).
     */
    public RestaurantTable findOptimalTable(int guestsCount, String time) {
        List<RestaurantTable> tables = tableRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByTime(time);

        // Brak pętli oraz instrukcji if - całe wyszukiwanie i sortowanie Best-Fit odbywa się w streamie
        return tables.stream()
            .filter(t -> t.getSeats() >= guestsCount)
            .filter(t -> reservations.stream().noneMatch(r -> r.getTableId().equals(t.getId())))
            .min(Comparator.comparingInt(RestaurantTable::getSeats))
            .orElse(null);
    }

    /**
     * Rezerwuje stolik (podejście funkcyjne bez if i pętli).
     */
    @Transactional
    public String bookTable(String userId, Long tableId, String time, int guestsCount) {
        return tableRepository.findById(tableId)
            .map(table -> reservationRepository.findByTableIdAndTime(tableId, time)
                .map(existingTable -> "Table already reserved")
                .orElseGet(() -> reservationRepository.findByUserIdAndTime(userId, time)
                    .map(existingUser -> "User already has a reservation at this time")
                    .orElseGet(() -> table.getSeats() < guestsCount 
                        ? "Not enough seats" 
                        : saveAndReturn(new Reservation(tableId, time, guestsCount, userId), "Reservation successful")))
            ).orElse("Table not found");
    }

    private String saveAndReturn(Reservation reservation, String message) {
        reservationRepository.save(reservation);
        return message;
    }
}
