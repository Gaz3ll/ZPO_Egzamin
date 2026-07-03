const { Sequelize } = require('sequelize');
const config = require('../config/config.json');

const sequelize = new Sequelize({
  dialect: config.dialect,
  storage: config.storage,
  logging: config.logging,
});

const FitnessClass = require('./fitnessClass')(sequelize);
const Registration = require('./registration')(sequelize);

module.exports = { sequelize, FitnessClass, Registration };
