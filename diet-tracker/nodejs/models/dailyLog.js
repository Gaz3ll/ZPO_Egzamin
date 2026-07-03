const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('DailyLog', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    date: { type: DataTypes.STRING, allowNull: false }, // YYYY-MM-DD
    mealId: { type: DataTypes.INTEGER, allowNull: false, field: 'meal_id' },
    userId: { type: DataTypes.STRING, allowNull: false, field: 'user_id' },
  }, { tableName: 'daily_logs', timestamps: false });
};
