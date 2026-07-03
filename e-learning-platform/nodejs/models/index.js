const { Sequelize } = require('sequelize');
const config = require('../config/config.json');

const sequelize = new Sequelize({
  dialect: config.dialect,
  storage: config.storage,
  logging: config.logging,
});

const Question = require('./question')(sequelize);
const QuizResult = require('./quizResult')(sequelize);

module.exports = { sequelize, Question, QuizResult };
