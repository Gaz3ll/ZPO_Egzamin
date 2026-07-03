const { Meal, DailyLog, UserProfile } = require('../models');

/**
 * Oblicza zapotrzebowanie kaloryczne (BMR) wzorem Harrisa-Benedicta (podejście funkcyjne).
 */
function calculateBmr(weight, height, age, gender) {
  return gender === 'M'
    ? 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
    : 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
}

/**
 * Sumuje makroskładniki i kalorie z tablicy posiłków (podejście funkcyjne z reduce).
 */
function calculateMacroSummary(meals) {
  return meals.reduce((acc, meal) => ({
    proteins: acc.proteins + meal.proteins,
    carbs: acc.carbs + meal.carbs,
    fats: acc.fats + meal.fats,
    calories: acc.calories + meal.calories
  }), { proteins: 0, carbs: 0, fats: 0, calories: 0 });
}

/**
 * Pobiera BMR użytkownika na podstawie jego profilu (podejście funkcyjne).
 */
async function getUserBmr(userId) {
  const profile = await UserProfile.findByPk(userId);
  return profile 
    ? calculateBmr(profile.weight, profile.height, profile.age, profile.gender)
    : 2000;
}

/**
 * Pobiera pełne podsumowanie dnia (podejście funkcyjne).
 */
async function getDailySummary(userId, date) {
  const logs = await DailyLog.findAll({ where: { userId, date } });
  const mealIds = logs.map(l => l.mealId);
  const meals = await Meal.findAll({ where: { id: mealIds } });
  
  const bmr = await getUserBmr(userId);
  const summary = calculateMacroSummary(meals);
  const percentage = bmr > 0 ? (summary.calories / bmr) * 100 : 0;

  return { bmr, ...summary, percentage };
}

module.exports = { calculateBmr, calculateMacroSummary, getUserBmr, getDailySummary };
