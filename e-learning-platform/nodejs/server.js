const express = require('express');
const path = require('path');
const { sequelize, Question } = require('./models');
const quizRoutes = require('./routes/quiz');
const teacherRoutes = require('./routes/teacher');

const app = express();
const PORT = process.env.PORT || 3000;

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'static')));

app.use(quizRoutes);
app.use(teacherRoutes);

async function seed() {
  const count = await Question.count();
  if (count === 0) {
    await Question.bulkCreate([
      { content: 'What is the capital of France?', correctAnswer: 'B', points: 1, optionA: 'London', optionB: 'Paris', optionC: 'Berlin', optionD: 'Madrid' },
      { content: 'What is 2 + 2?', correctAnswer: 'A', points: 1, optionA: '4', optionB: '5', optionC: '3', optionD: '6' },
      { content: 'Which planet is known as the Red Planet?', correctAnswer: 'C', points: 1, optionA: 'Venus', optionB: 'Jupiter', optionC: 'Mars', optionD: 'Saturn' },
      { content: 'What is the largest mammal?', correctAnswer: 'D', points: 1, optionA: 'Elephant', optionB: 'Giraffe', optionC: 'Shark', optionD: 'Blue Whale' },
      { content: 'Which gas do plants absorb?', correctAnswer: 'B', points: 1, optionA: 'Oxygen', optionB: 'Carbon Dioxide', optionC: 'Nitrogen', optionD: 'Hydrogen' },
    ]);
  }
}

sequelize.sync().then(seed).then(() => {
  app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
});

module.exports = app;
