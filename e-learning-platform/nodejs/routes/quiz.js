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
