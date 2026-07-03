package com.restaurant.controller;

import com.restaurant.model.RestaurantTable;
import com.restaurant.model.Reservation;
import com.restaurant.service.RestaurantService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public String showRestaurant(Authentication auth, Model model) {
        boolean isWaiter = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WAITER"));
        List<Reservation> reservations = isWaiter 
            ? restaurantService.getAllReservations() 
            : restaurantService.getReservationsByUser(auth.getName());

        model.addAttribute("reservations", reservations);
        model.addAttribute("user", auth.getName());
        model.addAttribute("isWaiter", isWaiter);
        model.addAttribute("searchResult", null);
        model.addAttribute("time", "");
        model.addAttribute("guestsCount", "");
        model.addAttribute("message", null);
        return "restaurant";
    }

    @PostMapping("/search")
    public String searchTable(Authentication auth, @RequestParam String time, @RequestParam int guestsCount, Model model) {
        RestaurantTable optimalTable = restaurantService.findOptimalTable(guestsCount, time);
        boolean isWaiter = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WAITER"));
        List<Reservation> reservations = isWaiter 
            ? restaurantService.getAllReservations() 
            : restaurantService.getReservationsByUser(auth.getName());

        model.addAttribute("reservations", reservations);
        model.addAttribute("user", auth.getName());
        model.addAttribute("isWaiter", isWaiter);
        model.addAttribute("searchResult", optimalTable);
        model.addAttribute("time", time);
        model.addAttribute("guestsCount", guestsCount);
        model.addAttribute("message", optimalTable != null ? null : "Brak wolnych stolików o podanej godzinie dla tylu osób.");
        return "restaurant";
    }

    @PostMapping("/book")
    public String bookTable(Authentication auth, @RequestParam Long tableId, @RequestParam String time, @RequestParam int guestsCount, Model model) {
        String message = restaurantService.bookTable(auth.getName(), tableId, time, guestsCount);
        List<Reservation> reservations = restaurantService.getReservationsByUser(auth.getName());

        model.addAttribute("reservations", reservations);
        model.addAttribute("user", auth.getName());
        model.addAttribute("isWaiter", false);
        model.addAttribute("searchResult", null);
        model.addAttribute("time", "");
        model.addAttribute("guestsCount", "");
        model.addAttribute("message", message);
        return "restaurant";
    }
}
