const express = require('express');
const router = express.Router();
const { QuizResult } = require('../models');
const { auth, requireRole } = require('../middleware/auth');

router.get('/teacher/results', auth, requireRole('TEACHER'), async (req, res) => {
  const results = await QuizResult.findAll();
  res.render('results', { results });
});

/**
 * @openapi
 * /teacher/api/results:
 *   get:
 *     summary: Pobierz wszystkie wyniki studentów
 *     description: Endpoint zwraca listę wszystkich wyników quizów. Dostępne tylko dla nauczycieli.
 *     security:
 *       - basicAuth: []
 *     responses:
 *       200:
 *         description: Lista wyników pobrana pomyślnie.
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   id:
 *                     type: integer
 *                   userId:
 *                     type: string
 *                   score:
 *                     type: number
 *                   percentage:
 *                     type: number
 *                   passed:
 *                     type: boolean
 */
router.get('/teacher/api/results', auth, requireRole('TEACHER'), async (req, res) => {
  const results = await QuizResult.findAll();
  res.json(results);
});

module.exports = router;
