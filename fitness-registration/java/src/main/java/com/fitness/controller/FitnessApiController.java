package com.fitness.controller;

import com.fitness.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fitness")
@Tag(name = "Fitness", description = "Fitness class registration API")
public class FitnessApiController {

    private final RegistrationService registrationService;

    public FitnessApiController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register/{classId}")
    @Operation(summary = "Register for a class")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> register(Authentication auth, @PathVariable Long classId) {
        String message = registrationService.register(auth.getName(), classId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/unregister/{classId}")
    @Operation(summary = "Unregister from a class", description = "Triggers waiting list logic")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> unregister(Authentication auth, @PathVariable Long classId) {
        String message = registrationService.unregister(auth.getName(), classId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
