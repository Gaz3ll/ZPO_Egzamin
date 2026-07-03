const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('UserProfile', {
    userId: { type: DataTypes.STRING, primaryKey: true, field: 'user_id' },
    weight: { type: DataTypes.FLOAT, allowNull: false },
    height: { type: DataTypes.FLOAT, allowNull: false },
    age: { type: DataTypes.INTEGER, allowNull: false },
    gender: { type: DataTypes.STRING, allowNull: false }, // M / F
  }, { tableName: 'user_profiles', timestamps: false });
};
