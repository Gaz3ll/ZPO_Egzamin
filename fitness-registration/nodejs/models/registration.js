const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('Registration', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    userId: { type: DataTypes.STRING, allowNull: false, field: 'user_id' },
    classId: { type: DataTypes.INTEGER, allowNull: false, field: 'class_id' },
    status: { type: DataTypes.STRING, allowNull: false },
    position: { type: DataTypes.INTEGER, allowNull: true },
  }, { tableName: 'registrations', timestamps: false });
};
