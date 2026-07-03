const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('Table', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    seats: { type: DataTypes.INTEGER, allowNull: false },
    location: { type: DataTypes.STRING, allowNull: false }, // INDOOR / OUTDOOR
  }, { tableName: 'tables', timestamps: false });
};
