const { Table, Reservation } = require('../models');

/**
 * Wyszukuje optymalny stolik dla określonej liczby gości i godziny (podejście funkcyjne).
 * Zwraca najmniejszy pasujący stolik, który nie jest w tym czasie zarezerwowany.
 */
async function findOptimalTable(guestsCount, time) {
  const tables = await Table.findAll();
  const reservations = await Reservation.findAll({ where: { time } });

  // Funkcyjne sprawdzenie czy stolik jest zajęty (bez pętli i ifów)
  const isReserved = (tableId) => reservations.some(r => r.tableId === tableId);

  // Filtrowanie stolików spełniających warunki pojemności i dostępności
  const available = tables.filter(t => t.seats >= guestsCount && !isReserved(t.id));

  // Sortowanie po liczbie miejsc (rosnąco) i wybór pierwszego optymalnego stolika (Best-Fit)
  return available.sort((a, b) => a.seats - b.seats)[0] || null;
}

/**
 * Dokonuje rezerwacji stolika (podejście funkcyjne bez instrukcji if).
 */
async function bookTable(userId, tableId, time, guestsCount) {
  const table = await Table.findByPk(tableId);
  const existing = await Reservation.findOne({ where: { tableId, time } });

  // Wyrażenia trójargumentowe z instrukcją przecinkową dla operacji zapisu bazy danych
  return !table 
    ? 'Table not found'
    : existing 
      ? 'Table already reserved'
      : table.seats < guestsCount 
        ? 'Not enough seats'
        : (await Reservation.create({ tableId, time, guestsCount, userId }), 'Reservation successful');
}

module.exports = { findOptimalTable, bookTable };
