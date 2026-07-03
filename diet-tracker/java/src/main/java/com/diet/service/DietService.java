package com.diet.service;

import com.diet.model.Meal;
import com.diet.model.DailyLog;
import com.diet.model.UserProfile;
import com.diet.repository.MealRepository;
import com.diet.repository.DailyLogRepository;
import com.diet.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis obliczający zapotrzebowanie BMR i sumujący posiłki.
 * Wszystkie algorytmy zostały napisane funkcyjnie (bez pętli oraz ifów).
 */
@Service
public class DietService {

    private final MealRepository mealRepository;
    private final DailyLogRepository dailyLogRepository;
    private final UserProfileRepository userProfileRepository;

    public DietService(MealRepository mealRepository, DailyLogRepository dailyLogRepository, UserProfileRepository userProfileRepository) {
        this.mealRepository = mealRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    public UserProfile getUserProfile(String userId) {
        return userProfileRepository.findById(userId)
            .orElse(new UserProfile(userId, 70.0, 175.0, 25, "M"));
    }

    @Transactional
    public void updateProfile(String userId, double weight, double height, int age, String gender) {
        userProfileRepository.save(new UserProfile(userId, weight, height, age, gender));
    }

    @Transactional
    public void addMealLog(String date, Long mealId, String userId) {
        dailyLogRepository.save(new DailyLog(date, mealId, userId));
    }

    /**
     * Wyliczanie zapotrzebowania BMR (podejście funkcyjne bez if).
     */
    public double calculateBmr(double weight, double height, int age, String gender) {
        return "M".equalsIgnoreCase(gender)
            ? 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
            : 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
    }

    /**
     * Generuje podsumowanie dnia (podejście funkcyjne bez pętli).
     */
    public DailySummaryResponse getDailySummary(String userId, String date) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDate(userId, date);
        
        // Pobieramy posiłki na podstawie logów za pomocą streamu
        List<Meal> meals = logs.stream()
            .map(log -> mealRepository.findById(log.getMealId()).orElse(null))
            .filter(meal -> meal != null)
            .toList();

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        
        double bmr = profile != null 
            ? calculateBmr(profile.getWeight(), profile.getHeight(), profile.getAge(), profile.getGender()) 
            : 2000.0;

        // Sumowanie wartości w sposób funkcyjny
        double proteins = meals.stream().mapToDouble(Meal::getProteins).sum();
        double carbs = meals.stream().mapToDouble(Meal::getCarbs).sum();
        double fats = meals.stream().mapToDouble(Meal::getFats).sum();
        double calories = meals.stream().mapToDouble(Meal::getCalories).sum();
        double percentage = bmr > 0 ? (calories / bmr) * 100.0 : 0.0;

        return new DailySummaryResponse(bmr, proteins, carbs, fats, calories, percentage);
    }

    public static class DailySummaryResponse {
        private final double bmr;
        private final double proteins;
        private final double carbs;
        private final double fats;
        private final double calories;
        private final double percentage;

        public DailySummaryResponse(double bmr, double proteins, double carbs, double fats, double calories, double percentage) {
            this.bmr = bmr;
            this.proteins = proteins;
            this.carbs = carbs;
            this.fats = fats;
            this.calories = calories;
            this.percentage = percentage;
        }

        public double getBmr() { return bmr; }
        public double getProteins() { return proteins; }
        public double getCarbs() { return carbs; }
        public double getFats() { return fats; }
        public double getCalories() { return calories; }
        public double getPercentage() { return percentage; }
    }
}
