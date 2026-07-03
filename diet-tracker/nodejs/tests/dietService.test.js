const { calculateBmr, calculateMacroSummary } = require('../services/dietService');

test('powinien poprawnie wyliczyć BMR dla mężczyzn', () => {
  // Waga 80kg, Wzrost 180cm, Wiek 30 lat, Mężczyzna
  // BMR = 88.362 + (13.397 * 80) + (4.799 * 180) - (5.677 * 30) = 88.362 + 1071.76 + 863.82 - 170.31 = 1853.632
  const bmr = calculateBmr(80, 180, 30, 'M');
  expect(bmr).toBeCloseTo(1853.632, 2);
});

test('powinien poprawnie wyliczyć BMR dla kobiet', () => {
  // Waga 60kg, Wzrost 165cm, Wiek 25 lat, Kobieta
  // BMR = 447.593 + (9.247 * 60) + (3.098 * 165) - (4.330 * 25) = 447.593 + 554.82 + 511.17 - 108.25 = 1405.333
  const bmr = calculateBmr(60, 165, 25, 'F');
  expect(bmr).toBeCloseTo(1405.333, 2);
});

test('powinien poprawnie zsumować makroskładniki i kalorie', () => {
  const meals = [
    { name: 'Meal 1', proteins: 20, carbs: 50, fats: 10, calories: 370 },
    { name: 'Meal 2', proteins: 30, carbs: 10, fats: 15, calories: 295 },
  ];

  const summary = calculateMacroSummary(meals);
  expect(summary.proteins).toBe(50);
  expect(summary.carbs).toBe(60);
  expect(summary.fats).toBe(25);
  expect(summary.calories).toBe(665);
});
