const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('Meal', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    name: { type: DataTypes.STRING, allowNull: false },
    proteins: { type: DataTypes.FLOAT, allowNull: false },
    carbs: { type: DataTypes.FLOAT, allowNull: false },
    fats: { type: DataTypes.FLOAT, allowNull: false },
    calories: { type: DataTypes.FLOAT, allowNull: false },
  }, { tableName: 'meals', timestamps: false });
};
