const { findOptimalTable, bookTable } = require('../services/restaurantService');
const { sequelize, Table, Reservation } = require('../models');

beforeAll(async () => {
  await sequelize.sync({ force: true });
  
  // Seedowanie stolików o różnych rozmiarach
  await Table.bulkCreate([
    { id: 1, seats: 2, location: 'INDOOR' },
    { id: 2, seats: 4, location: 'INDOOR' },
    { id: 3, seats: 4, location: 'OUTDOOR' },
    { id: 4, seats: 8, location: 'INDOOR' },
  ]);
});

afterEach(async () => {
  await Reservation.destroy({ where: {} });
});

afterAll(async () => {
  await sequelize.close();
});

test('powinien dobrać najmniejszy wolny stolik pasujący do liczby gości (Best-Fit)', async () => {
  // Szukamy stolika dla 3 osób o 18:00
  // Powinien wybrać stolik 4-osobowy (id: 2 lub 3), a nie blokować 8-osobowego (id: 4) lub za małego 2-osobowego (id: 1)
  const optimal = await findOptimalTable(3, '18:00');
  expect(optimal).not.toBeNull();
  expect(optimal.seats).toBe(4);
  expect([2, 3]).toContain(optimal.id);
});

test('nie powinien dobrać zajętego stolika o podanej godzinie', async () => {
  // Zarezerwujmy stoliki 4-osobowe o 19:00
  await Reservation.create({ tableId: 2, time: '19:00', guestsCount: 3, userId: 'guest1' });
  await Reservation.create({ tableId: 3, time: '19:00', guestsCount: 4, userId: 'guest2' });

  // Szukamy dla 3 osób o 19:00. Skoro 4-osobowe są zajęte, optymalny musi być stolik 8-osobowy (id: 4)
  const optimal = await findOptimalTable(3, '19:00');
  expect(optimal).not.toBeNull();
  expect(optimal.id).toBe(4);
});

test('powinien zwrócić null jeśli nie ma stolika o wystarczającej pojemności', async () => {
  // Szukamy stolika dla 10 osób
  const optimal = await findOptimalTable(10, '20:00');
  expect(optimal).toBeNull();
});

test('powinien poprawnie zarezerwować stolik oraz odrzucić rezerwację na zajęty czas', async () => {
  const result1 = await bookTable('guest1', 1, '20:00', 2);
  expect(result1).toBe('Reservation successful');

  const result2 = await bookTable('guest2', 1, '20:00', 2);
  expect(result2).toBe('Table already reserved');
});
