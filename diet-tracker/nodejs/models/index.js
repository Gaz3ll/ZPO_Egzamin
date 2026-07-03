const { Sequelize } = require('sequelize');
const config = require('../config/config.json');

const sequelize = new Sequelize({
  dialect: config.dialect,
  storage: config.storage,
  logging: config.logging,
});

const Meal = require('./meal')(sequelize);
const DailyLog = require('./dailyLog')(sequelize);
const UserProfile = require('./userProfile')(sequelize);

module.exports = { sequelize, Meal, DailyLog, UserProfile };
