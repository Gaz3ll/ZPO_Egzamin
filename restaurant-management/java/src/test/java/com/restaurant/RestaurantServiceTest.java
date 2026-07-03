package com.restaurant;

import com.restaurant.model.RestaurantTable;
import com.restaurant.model.Reservation;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RestaurantServiceTest {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantService restaurantService;

    @BeforeEach
    public void setup() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();

        // Seedowanie stolików o różnych rozmiarach
        tableRepository.save(new RestaurantTable(2, "INDOOR"));
        tableRepository.save(new RestaurantTable(4, "INDOOR"));
        tableRepository.save(new RestaurantTable(4, "OUTDOOR"));
        tableRepository.save(new RestaurantTable(8, "INDOOR"));
    }

    @Test
    public void testOptimalTableBestFit() {
        // Szukamy stolika dla 3 osób o 18:00
        // Powinien wybrać stolik 4-osobowy, a nie 8-osobowy lub za mały 2-osobowy
        RestaurantTable optimal = restaurantService.findOptimalTable(3, "18:00");
        assertNotNull(optimal);
        assertEquals(4, optimal.getSeats());
    }

    @Test
    public void testReservedTableSkip() {
        // Pobieramy stoliki
        List<RestaurantTable> tables = tableRepository.findAll();
        RestaurantTable t4_1 = tables.stream().filter(t -> t.getSeats() == 4).toList().get(0);
        RestaurantTable t4_2 = tables.stream().filter(t -> t.getSeats() == 4).toList().get(1);

        // Zarezerwujmy oba stoliki 4-osobowe o 19:00
        reservationRepository.save(new Reservation(t4_1.getId(), "19:00", 3, "guest1"));
        reservationRepository.save(new Reservation(t4_2.getId(), "19:00", 4, "guest2"));

        // Szukamy dla 3 osób o 19:00. Skoro stoliki 4-osobowe są zajęte, optymalny musi być stolik 8-osobowy
        RestaurantTable optimal = restaurantService.findOptimalTable(3, "19:00");
        assertNotNull(optimal);
        assertEquals(8, optimal.getSeats());
    }

    @Test
    public void testNoMatchingTable() {
        // Szukamy stolika dla 10 osób
        RestaurantTable optimal = restaurantService.findOptimalTable(10, "20:00");
        assertNull(optimal);
    }

    @Test
    public void testBookTablePreventDuplicates() {
        List<RestaurantTable> tables = tableRepository.findAll();
        Long tId = tables.get(0).getId();

        // Pierwsza rezerwacja przechodzi
        String result1 = restaurantService.bookTable("guest1", tId, "20:00", 2);
        assertEquals("Reservation successful", result1);

        // Druga na ten sam stolik i czas zostaje odrzucona
        String result2 = restaurantService.bookTable("guest2", tId, "20:00", 2);
        assertEquals("Table already reserved", result2);
    }

    @Test
    public void testBookTablePreventUserDuplicates() {
        List<RestaurantTable> tables = tableRepository.findAll();
        Long tId1 = tables.get(0).getId();
        Long tId2 = tables.get(1).getId();

        // Pierwsza rezerwacja przechodzi
        String result1 = restaurantService.bookTable("guest1", tId1, "20:00", 2);
        assertEquals("Reservation successful", result1);

        // Druga rezerwacja tego samego użytkownika o tej samej godzinie zostaje odrzucona
        String result2 = restaurantService.bookTable("guest1", tId2, "20:00", 4);
        assertEquals("User already has a reservation at this time", result2);
    }
}
