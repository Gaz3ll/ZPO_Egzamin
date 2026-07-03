const express = require('express');
const router = express.Router();
const { Question, QuizResult } = require('../models');
const { calculateScore, calculatePercentage, isPassed } = require('../services/scoreCalculator');
const { auth, requireRole } = require('../middleware/auth');

router.get('/quiz', auth, requireRole('STUDENT'), async (req, res) => {
  const questions = await Question.findAll();
  res.render('quiz', { questions });
});

router.post('/quiz/submit', auth, requireRole('STUDENT'), async (req, res) => {
  const questions = await Question.findAll();
  let correct = 0, wrong = 0;
  for (const q of questions) {
    const answer = req.body[String(q.id)];
    if (!answer) continue;
    if (String(answer).trim() === q.correctAnswer) correct++;
    else wrong++;
  }
  const score = calculateScore(questions.length, correct, wrong);
  const percentage = calculatePercentage(score, questions.length);
  const passed = isPassed(percentage);
  await QuizResult.create({ userId: req.auth.user, score, percentage, passed });
  res.render('summary', { result: { score, percentage, passed, correct, wrong, total: questions.length } });
});

/**
 * @openapi
 * /api/quiz/submit:
 *   post:
 *     summary: Prześlij odpowiedzi na quiz
 *     description: Endpoint przyjmuje odpowiedzi studenta na quiz, oblicza wynik procentowy i status zaliczenia. Dostępne tylko dla studentów.
 *     security:
 *       - basicAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               answers:
 *                 type: object
 *                 additionalProperties:
 *                   type: string
 *                 example:
 *                   "1": "B"
 *                   "2": "A"
 *     responses:
 *       200:
 *         description: Wynik quizu obliczony pomyślnie.
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 score:
 *                   type: number
 *                 percentage:
 *                   type: number
 *                 passed:
 *                   type: boolean
 *                 correct:
 *                   type: integer
 *                 wrong:
 *                   type: integer
 *                 total:
 *                   type: integer
 */
router.post('/api/quiz/submit', auth, requireRole('STUDENT'), async (req, res) => {
  const questions = await Question.findAll();
  let correct = 0, wrong = 0;
  for (const q of questions) {
    const answer = req.body.answers ? req.body.answers[String(q.id)] : null;
    if (!answer) continue;
    if (String(answer).trim() === q.correctAnswer) correct++;
    else wrong++;
  }
  const score = calculateScore(questions.length, correct, wrong);
  const percentage = calculatePercentage(score, questions.length);
  const passed = isPassed(percentage);
  await QuizResult.create({ userId: req.auth.user, score, percentage, passed });
  res.json({ score, percentage, passed, correct, wrong, total: questions.length });
});

module.exports = router;
