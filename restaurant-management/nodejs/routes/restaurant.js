const express = require('express');
const router = express.Router();
const { Table, Reservation } = require('../models');
const { findOptimalTable, bookTable } = require('../services/restaurantService');
const { auth, requireRole } = require('../middleware/auth');

// Widok panelu rezerwacji dla gościa i kelnera
router.get('/restaurant', auth, async (req, res) => {
  const isWaiter = req.auth.user === 'waiter1'; // Kelner widzi wszystkie rezerwacje
  const reservations = isWaiter 
    ? await Reservation.findAll() 
    : await Reservation.findAll({ where: { userId: req.auth.user } });
  
  res.render('restaurant', { 
    reservations, 
    user: req.auth.user, 
    isWaiter,
    searchResult: null,
    time: '',
    guestsCount: '',
    message: null 
  });
});

// Obsługa wyszukiwania wolnego stolika przez UI
router.post('/restaurant/search', auth, async (req, res) => {
  const time = req.body.time;
  const guestsCount = parseInt(req.body.guestsCount) || 0;
  
  const optimalTable = await findOptimalTable(guestsCount, time);
  const isWaiter = req.auth.user === 'waiter1';
  const reservations = isWaiter 
    ? await Reservation.findAll() 
    : await Reservation.findAll({ where: { userId: req.auth.user } });

  res.render('restaurant', {
    reservations,
    user: req.auth.user,
    isWaiter,
    searchResult: optimalTable,
    time,
    guestsCount,
    message: optimalTable ? null : 'Brak wolnych stolików o podanej godzinie dla tylu osób.'
  });
});

// Zarezerwowanie wybranego stolika przez UI
router.post('/restaurant/book', auth, requireRole('GUEST'), async (req, res) => {
  const { tableId, time, guestsCount } = req.body;
  const message = await bookTable(req.auth.user, parseInt(tableId), time, parseInt(guestsCount));
  
  const reservations = await Reservation.findAll({ where: { userId: req.auth.user } });

  res.render('restaurant', {
    reservations,
    user: req.auth.user,
    isWaiter: false,
    searchResult: null,
    time: '',
    guestsCount: '',
    message
  });
});

/**
 * @openapi
 * /api/restaurant/tables/search:
 *   get:
 *     summary: Znajdź optymalny stolik
 *     description: Wyszukuje pierwszy pasujący stolik spełniający warunki pojemności oraz braku rezerwacji w podanym czasie (Best-Fit).
 *     security:
 *       - basicAuth: []
 *     parameters:
 *       - in: query
 *         name: guestsCount
 *         required: true
 *         schema:
 *           type: integer
 *         description: Liczba gości
 *       - in: query
 *         name: time
 *         required: true
 *         schema:
 *           type: string
 *         description: Godzina rezerwacji (np. 18:00)
 *     responses:
 *       200:
 *         description: Pomyślnie znaleziono lub brak pasującego stolika.
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 table:
 *                   type: object
 *                   nullable: true
 *                   properties:
 *                     id:
 *                       type: integer
 *                     seats:
 *                       type: integer
 *                     location:
 *                       type: string
 */
router.get('/api/restaurant/tables/search', auth, async (req, res) => {
  const guestsCount = parseInt(req.query.guestsCount) || 0;
  const time = req.query.time || '';
  const table = await findOptimalTable(guestsCount, time);
  res.json({ table });
});

module.exports = router;
