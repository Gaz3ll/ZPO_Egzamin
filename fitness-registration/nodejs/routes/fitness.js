const express = require('express');
const router = express.Router();
const { FitnessClass } = require('../models');
const { register, unregister } = require('../services/registrationService');
const { auth } = require('../middleware/auth');

router.get('/fitness', auth, async (req, res) => {
  const classes = await FitnessClass.findAll();
  res.render('fitness', { classes, message: null });
});

router.post('/fitness/register/:classId', auth, async (req, res) => {
  const message = await register(req.auth.user, parseInt(req.params.classId));
  const classes = await FitnessClass.findAll();
  res.render('fitness', { classes, message });
});

router.post('/fitness/unregister/:classId', auth, async (req, res) => {
  const message = await unregister(req.auth.user, parseInt(req.params.classId));
  const classes = await FitnessClass.findAll();
  res.render('fitness', { classes, message });
});

/**
 * @openapi
 * /api/fitness/register/{classId}:
 *   post:
 *     summary: Zapisz się na zajęcia fitness
 *     description: Rejestruje zalogowanego użytkownika na zajęcia o podanym identyfikatorze classId. Jeśli limit miejsc jest wyczerpany, użytkownik trafia na listę rezerwową.
 *     security:
 *       - basicAuth: []
 *     parameters:
 *       - in: path
 *         name: classId
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID zajęć fitness
 *     responses:
 *       200:
 *         description: Wynik rejestracji.
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post('/api/fitness/register/:classId', auth, async (req, res) => {
  const message = await register(req.auth.user, parseInt(req.params.classId));
  res.json({ message });
});

/**
 * @openapi
 * /api/fitness/unregister/{classId}:
 *   post:
 *     summary: Wypisz się z zajęć fitness
 *     description: Wypisuje użytkownika z zajęć. Uruchamia logikę kolejkowania (promocja z listy rezerwowej lub przenumerowanie kolejki).
 *     security:
 *       - basicAuth: []
 *     parameters:
 *       - in: path
 *         name: classId
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID zajęć fitness
 *     responses:
 *       200:
 *         description: Wynik wyrejestrowania.
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post('/api/fitness/unregister/:classId', auth, async (req, res) => {
  const message = await unregister(req.auth.user, parseInt(req.params.classId));
  res.json({ message });
});

module.exports = router;
