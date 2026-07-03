const { Sequelize } = require('sequelize');
const config = require('../config/config.json');

const sequelize = new Sequelize({
  dialect: config.dialect,
  storage: config.storage,
  logging: config.logging,
});

const Table = require('./table')(sequelize);
const Reservation = require('./reservation')(sequelize);

module.exports = { sequelize, Table, Reservation };
