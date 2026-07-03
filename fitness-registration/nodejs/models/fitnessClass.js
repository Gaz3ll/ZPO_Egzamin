const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('FitnessClass', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    type: { type: DataTypes.STRING, allowNull: false },
    dayOfWeek: { type: DataTypes.STRING, allowNull: false, field: 'day_of_week' },
    time: { type: DataTypes.STRING, allowNull: false },
    maxCapacity: { type: DataTypes.INTEGER, allowNull: false, field: 'max_capacity' },
  }, { tableName: 'fitness_classes', timestamps: false });
};
