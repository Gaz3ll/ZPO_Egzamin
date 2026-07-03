const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('QuizResult', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    userId: { type: DataTypes.STRING, allowNull: false, field: 'user_id' },
    score: { type: DataTypes.FLOAT, allowNull: false },
    percentage: { type: DataTypes.FLOAT, allowNull: false },
    passed: { type: DataTypes.BOOLEAN, allowNull: false },
  }, { tableName: 'quiz_results', timestamps: false });
};
