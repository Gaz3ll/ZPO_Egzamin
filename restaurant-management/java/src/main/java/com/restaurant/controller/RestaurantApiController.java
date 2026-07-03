package com.restaurant.controller;

import com.restaurant.model.RestaurantTable;
import com.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
@Tag(name = "Restaurant", description = "Restaurant table reservation API")
public class RestaurantApiController {

    private final RestaurantService restaurantService;

    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/tables/search")
    @Operation(summary = "Search for an optimal table", description = "Returns the smallest available table for the given number of guests and time (Best-Fit)")
    public ResponseEntity<Map<String, RestaurantTable>> searchTable(@RequestParam int guestsCount, @RequestParam String time) {
        RestaurantTable table = restaurantService.findOptimalTable(guestsCount, time);
        Map<String, RestaurantTable> response = new java.util.HashMap<>();
        response.put("table", table);
        return ResponseEntity.ok(response);
    }
}
