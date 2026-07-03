package com.fitness.controller;

import com.fitness.model.FitnessClass;
import com.fitness.model.Registration;
import com.fitness.service.RegistrationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/fitness")
public class FitnessController {

    private final RegistrationService registrationService;

    public FitnessController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String showClasses(Model model) {
        List<FitnessClass> classes = registrationService.getAllClasses();
        model.addAttribute("classes", classes);
        return "fitness";
    }

    @PostMapping("/register/{classId}")
    public String register(Authentication auth, @PathVariable Long classId, Model model) {
        String message = registrationService.register(auth.getName(), classId);
        model.addAttribute("message", message);
        return showClasses(model);
    }

    @PostMapping("/unregister/{classId}")
    public String unregister(Authentication auth, @PathVariable Long classId, Model model) {
        String message = registrationService.unregister(auth.getName(), classId);
        model.addAttribute("message", message);
        return showClasses(model);
    }
}
