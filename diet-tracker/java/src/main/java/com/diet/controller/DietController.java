package com.diet.controller;

import com.diet.model.Meal;
import com.diet.model.DailyLog;
import com.diet.model.UserProfile;
import com.diet.service.DietService;
import com.diet.repository.MealRepository;
import com.diet.repository.DailyLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;
    private final MealRepository mealRepository;
    private final DailyLogRepository dailyLogRepository;

    public DietController(DietService dietService, MealRepository mealRepository, DailyLogRepository dailyLogRepository) {
        this.dietService = dietService;
        this.mealRepository = mealRepository;
        this.dailyLogRepository = dailyLogRepository;
    }

    @GetMapping
    public String showDiet(Authentication auth, @RequestParam(required = false) String date, Model model) {
        String targetDate = date != null ? date : LocalDate.now().toString();
        UserProfile profile = dietService.getUserProfile(auth.getName());
        List<Meal> meals = dietService.getAllMeals();

        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDate(auth.getName(), targetDate);
        List<Meal> loggedMeals = logs.stream()
            .map(log -> mealRepository.findById(log.getMealId()).orElse(null))
            .filter(m -> m != null)
            .toList();

        DietService.DailySummaryResponse summary = dietService.getDailySummary(auth.getName(), targetDate);

        model.addAttribute("user", auth.getName());
        model.addAttribute("profile", profile);
        model.addAttribute("meals", meals);
        model.addAttribute("loggedMeals", loggedMeals);
        model.addAttribute("date", targetDate);
        model.addAttribute("summary", summary);
        return "diet";
    }

    @PostMapping("/meal/add")
    public String addMeal(Authentication auth, @RequestParam String date, @RequestParam Long mealId) {
        dietService.addMealLog(date, mealId, auth.getName());
        return "redirect:/diet?date=" + date;
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth, @RequestParam String date, @RequestParam double weight,
                                @RequestParam double height, @RequestParam int age, @RequestParam String gender) {
        dietService.updateProfile(auth.getName(), weight, height, age, gender);
        return "redirect:/diet?date=" + date;
    }
}
