from models import SessionLocal, Meal, DailyLog, UserProfile

def calculate_bmr(weight: float, height: float, age: int, gender: str) -> float:
    """
    Oblicza BMR wzorem Harrisa-Benedicta (podejście funkcyjne bez instrukcji if).
    """
    return (88.362 + 13.397 * weight + 4.799 * height - 5.677 * age) if gender == "M" else \
           (447.593 + 9.247 * weight + 3.098 * height - 4.330 * age)


def calculate_macro_summary(meals: list) -> dict:
    """
    Sumuje makroskładniki i kalorie posiłków w sposób funkcyjny (bez pętli for).
    """
    return {
        "proteins": sum(map(lambda m: m.proteins, meals)),
        "carbs": sum(map(lambda m: m.carbs, meals)),
        "fats": sum(map(lambda m: m.fats, meals)),
        "calories": sum(map(lambda m: m.calories, meals)),
    }


def get_user_bmr(user_id: str) -> float:
    db = SessionLocal()
    try:
        profile = db.query(UserProfile).filter(UserProfile.user_id == user_id).first()
        return calculate_bmr(profile.weight, profile.height, profile.age, profile.gender) if profile else 2000.0
    finally:
        db.close()


def get_daily_summary(user_id: str, date: str) -> dict:
    db = SessionLocal()
    try:
        logs = db.query(DailyLog).filter(DailyLog.user_id == user_id, DailyLog.date == date).all()
        meal_ids = list(map(lambda l: l.meal_id, logs))
        meals = db.query(Meal).filter(Meal.id.in_(meal_ids)).all() if meal_ids else []
        
        bmr = get_user_bmr(user_id)
        summary = calculate_macro_summary(meals)
        pct = (summary["calories"] / bmr * 100.0) if bmr > 0 else 0.0
        
        return {
            "bmr": bmr,
            "proteins": summary["proteins"],
            "carbs": summary["carbs"],
            "fats": summary["fats"],
            "calories": summary["calories"],
            "percentage": pct
        }
    finally:
        db.close()
