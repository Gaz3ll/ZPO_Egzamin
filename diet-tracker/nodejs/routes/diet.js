const express = require('express');
const router = express.Router();
const { Meal, DailyLog, UserProfile } = require('../models');
const { getDailySummary } = require('../services/dietService');
const { auth } = require('../middleware/auth');

// Widok główny aplikacji dietetycznej
router.get('/diet', auth, async (req, res) => {
  const today = new Date().toISOString().split('T')[0];
  const date = req.query.date || today;

  const profile = await UserProfile.findByPk(req.auth.user) || { weight: 70, height: 175, age: 25, gender: 'M' };
  const meals = await Meal.findAll();
  
  // Pobieramy posiłki przypisane do danego dnia
  const logs = await DailyLog.findAll({ where: { userId: req.auth.user, date } });
  const loggedMeals = await Meal.findAll({ where: { id: logs.map(l => l.mealId) } });

  const summary = await getDailySummary(req.auth.user, date);

  res.render('diet', {
    user: req.auth.user,
    profile,
    meals,
    loggedMeals,
    date,
    summary,
    message: null
  });
});

// Dodanie posiłku do dziennika
router.post('/diet/meal/add', auth, async (req, res) => {
  const { date, mealId } = req.body;
  await DailyLog.create({ date, mealId: parseInt(mealId), userId: req.auth.user });
  res.redirect(`/diet?date=${date}`);
});

// Aktualizacja parametrów wagi/wzrostu/wieku/płci
router.post('/diet/profile/update', auth, async (req, res) => {
  const { weight, height, age, gender, date } = req.body;
  
  await UserProfile.upsert({
    userId: req.auth.user,
    weight: parseFloat(weight),
    height: parseFloat(height),
    age: parseInt(age),
    gender
  });

  res.redirect(`/diet?date=${date}`);
});

/**
 * @openapi
 * /api/diet/logs/summary:
 *   get:
 *     summary: Pobierz podsumowanie makroskładników
 *     description: Pobiera podsumowanie dziennego zapotrzebowania (BMR), spożytych kalorii i makroskładników dla zalogowanego użytkownika w wybranym dniu.
 *     security:
 *       - basicAuth: []
 *     parameters:
 *       - in: query
 *         name: date
 *         required: true
 *         schema:
 *           type: string
 *         description: Data w formacie YYYY-MM-DD
 *     responses:
 *       200:
 *         description: Pomyślnie pobrano podsumowanie.
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 bmr:
 *                   type: number
 *                 proteins:
 *                   type: number
 *                 carbs:
 *                   type: number
 *                 fats:
 *                   type: number
 *                 calories:
 *                   type: number
 *                 percentage:
 *                   type: number
 */
router.get('/api/diet/logs/summary', auth, async (req, res) => {
  const date = req.query.date || new Date().toISOString().split('T')[0];
  const summary = await getDailySummary(req.auth.user, date);
  res.json(summary);
});

module.exports = router;
