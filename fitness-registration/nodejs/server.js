const express = require('express');
const path = require('path');
const { sequelize, FitnessClass } = require('./models');
const fitnessRoutes = require('./routes/fitness');

const app = express();
const PORT = process.env.PORT || 3001;

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'static')));

app.use(fitnessRoutes);

async function seed() {
  const count = await FitnessClass.count();
  if (count === 0) {
    await FitnessClass.bulkCreate([
      { type: 'Yoga', dayOfWeek: 'Monday', time: '08:00', maxCapacity: 20 },
      { type: 'Pilates', dayOfWeek: 'Monday', time: '10:00', maxCapacity: 20 },
      { type: 'Spinning', dayOfWeek: 'Tuesday', time: '09:00', maxCapacity: 20 },
      { type: 'Zumba', dayOfWeek: 'Wednesday', time: '17:00', maxCapacity: 20 },
      { type: 'CrossFit', dayOfWeek: 'Thursday', time: '18:00', maxCapacity: 20 },
      { type: 'Boxing', dayOfWeek: 'Friday', time: '19:00', maxCapacity: 20 },
    ]);
  }
}

sequelize.sync().then(seed).then(() => {
  app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
});

module.exports = app;
