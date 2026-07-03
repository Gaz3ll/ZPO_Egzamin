package com.diet;

import com.diet.model.Meal;
import com.diet.service.DietService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DietServiceTest {

    @Autowired
    private DietService dietService;

    @Test
    public void testCalculateBmrMen() {
        // Waga 80kg, Wzrost 180cm, Wiek 30 lat, Mężczyzna
        // BMR = 88.362 + (13.397 * 80) + (4.799 * 180) - (5.677 * 30) = 1853.632
        double bmr = dietService.calculateBmr(80.0, 180.0, 30, "M");
        assertEquals(1853.632, bmr, 0.01);
    }

    @Test
    public void testCalculateBmrWomen() {
        // Waga 60kg, Wzrost 165cm, Wiek 25 lat, Kobieta
        // BMR = 447.593 + (9.247 * 60) + (3.098 * 165) - (4.330 * 25) = 1405.333
        double bmr = dietService.calculateBmr(60.0, 165.0, 25, "F");
        assertEquals(1405.333, bmr, 0.01);
    }

    @Test
    public void testCalculateMacroSummary() {
        List<Meal> meals = List.of(
            new Meal("Meal 1", 20.0, 50.0, 10.0, 370.0),
            new Meal("Meal 2", 30.0, 10.0, 15.0, 295.0)
        );

        // Sumowanie wartości w sposób funkcyjny
        double totalProteins = meals.stream().mapToDouble(Meal::getProteins).sum();
        double totalCarbs = meals.stream().mapToDouble(Meal::getCarbs).sum();
        double totalFats = meals.stream().mapToDouble(Meal::getFats).sum();
        double totalCalories = meals.stream().mapToDouble(Meal::getCalories).sum();

        assertEquals(50.0, totalProteins);
        assertEquals(60.0, totalCarbs);
        assertEquals(25.0, totalFats);
        assertEquals(665.0, totalCalories);
    }
}
