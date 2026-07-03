const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('Question', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    content: { type: DataTypes.STRING, allowNull: false },
    correctAnswer: { type: DataTypes.STRING, allowNull: false, field: 'correct_answer' },
    points: { type: DataTypes.INTEGER, defaultValue: 1 },
    optionA: { type: DataTypes.STRING, allowNull: false, field: 'option_a' },
    optionB: { type: DataTypes.STRING, allowNull: false, field: 'option_b' },
    optionC: { type: DataTypes.STRING, allowNull: false, field: 'option_c' },
    optionD: { type: DataTypes.STRING, allowNull: false, field: 'option_d' },
  }, { tableName: 'questions', timestamps: false });
};
