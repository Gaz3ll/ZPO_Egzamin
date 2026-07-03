package com.diet.controller;

import com.diet.service.DietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diet")
@Tag(name = "Diet", description = "Diet tracker and macro summary API")
public class DietApiController {

    private final DietService dietService;

    public DietApiController(DietService dietService) {
        this.dietService = dietService;
    }

    @GetMapping("/logs/summary")
    @Operation(summary = "Get daily macronutrients summary", description = "Calculates BMR and total calories/macros for the user on a specific date")
    public ResponseEntity<DietService.DailySummaryResponse> getSummary(Authentication auth, @RequestParam(required = false) String date) {
        String targetDate = date != null ? date : LocalDate.now().toString();
        DietService.DailySummaryResponse summary = dietService.getDailySummary(auth.getName(), targetDate);
        return ResponseEntity.ok(summary);
    }
}
