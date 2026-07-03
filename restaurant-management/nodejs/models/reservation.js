const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  return sequelize.define('Reservation', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    tableId: { type: DataTypes.INTEGER, allowNull: false, field: 'table_id' },
    time: { type: DataTypes.STRING, allowNull: false }, // HH:MM
    guestsCount: { type: DataTypes.INTEGER, allowNull: false, field: 'guests_count' },
    userId: { type: DataTypes.STRING, allowNull: false, field: 'user_id' },
  }, { tableName: 'reservations', timestamps: false });
};
