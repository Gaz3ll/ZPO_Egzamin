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

router.post('/api/fitness/register/:classId', auth, async (req, res) => {
  const message = await register(req.auth.user, parseInt(req.params.classId));
  res.json({ message });
});

router.post('/api/fitness/unregister/:classId', auth, async (req, res) => {
  const message = await unregister(req.auth.user, parseInt(req.params.classId));
  res.json({ message });
});

module.exports = router;
