import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.diet_service import calculate_bmr, calculate_macro_summary
from models import Meal


def test_calculate_bmr_men():
    # Waga 80kg, Wzrost 180cm, Wiek 30 lat, Mężczyzna
    # BMR = 88.362 + (13.397 * 80) + (4.799 * 180) - (5.677 * 30) = 1853.632
    bmr = calculate_bmr(80.0, 180.0, 30, "M")
    assert abs(bmr - 1853.632) < 0.01


def test_calculate_bmr_women():
    # Waga 60kg, Wzrost 165cm, Wiek 25 lat, Kobieta
    # BMR = 447.593 + (9.247 * 60) + (3.098 * 165) - (4.330 * 25) = 1405.333
    bmr = calculate_bmr(60.0, 165.0, 25, "F")
    assert abs(bmr - 1405.333) < 0.01


def test_calculate_macro_summary():
    meals = [
        Meal(name="Meal 1", proteins=20.0, carbs=50.0, fats=10.0, calories=370.0),
        Meal(name="Meal 2", proteins=30.0, carbs=10.0, fats=15.0, calories=295.0),
    ]
    summary = calculate_macro_summary(meals)
    assert summary["proteins"] == 50.0
    assert summary["carbs"] == 60.0
    assert summary["fats"] == 25.0
    assert summary["calories"] == 665.0
