const express = require('express');
const router = express.Router();
const { QuizResult } = require('../models');
const { auth, requireRole } = require('../middleware/auth');

router.get('/teacher/results', auth, requireRole('TEACHER'), async (req, res) => {
  const results = await QuizResult.findAll();
  res.render('results', { results });
});

router.get('/teacher/api/results', auth, requireRole('TEACHER'), async (req, res) => {
  const results = await QuizResult.findAll();
  res.json(results);
});

module.exports = router;
