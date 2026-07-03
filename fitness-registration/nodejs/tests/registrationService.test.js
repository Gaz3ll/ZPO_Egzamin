const { register, unregister } = require('../services/registrationService');
const { sequelize, FitnessClass, Registration } = require('../models');

beforeAll(async () => {
  await sequelize.sync({ force: true });
  await FitnessClass.create({ type: 'Yoga', dayOfWeek: 'Monday', time: '08:00', maxCapacity: 2 });
});

afterEach(async () => {
  await Registration.destroy({ where: {} });
});

afterAll(async () => {
  await sequelize.close();
});

test('register on main list', async () => {
  const msg = await register('user1', 1);
  expect(msg).toBe('Registered on main list');
  const count = await Registration.count({ where: { classId: 1, status: 'MAIN' } });
  expect(count).toBe(1);
});

test('register on waiting list', async () => {
  await register('user1', 1);
  await register('user2', 1);
  const msg = await register('user3', 1);
  expect(msg).toBe('Registered on waiting list, position 1');
  const count = await Registration.count({ where: { classId: 1, status: 'WAITING' } });
  expect(count).toBe(1);
});

test('promotion from waiting list', async () => {
  await register('user1', 1);
  await register('user2', 1);
  await register('user3', 1);
  await register('user4', 1);

  expect(await Registration.count({ where: { classId: 1, status: 'MAIN' } })).toBe(2);
  expect(await Registration.count({ where: { classId: 1, status: 'WAITING' } })).toBe(2);

  const msg = await unregister('user1', 1);
  expect(msg).toBe('Unregistered from main list');

  const mainList = await Registration.findAll({ where: { classId: 1, status: 'MAIN' }, order: [['id', 'ASC']] });
  expect(mainList.length).toBe(2);
  expect(mainList[1].userId).toBe('user3');

  const waitingList = await Registration.findAll({ where: { classId: 1, status: 'WAITING' }, order: [['position', 'ASC']] });
  expect(waitingList.length).toBe(1);
  expect(waitingList[0].userId).toBe('user4');
  expect(waitingList[0].position).toBe(1);
});

test('unregister from waiting list', async () => {
  await register('user1', 1);
  await register('user2', 1);
  await register('user3', 1);

  const msg = await unregister('user3', 1);
  expect(msg).toBe('Unregistered from waiting list');

  const waitingList = await Registration.findAll({ where: { classId: 1, status: 'WAITING' }, order: [['position', 'ASC']] });
  expect(waitingList.length).toBe(0);
});

test('multiple promotions', async () => {
  await register('user1', 1);
  await register('user2', 1);
  await register('user3', 1);
  await register('user4', 1);

  await unregister('user1', 1);
  await unregister('user2', 1);

  const mainList = await Registration.findAll({ where: { classId: 1, status: 'MAIN' }, order: [['id', 'ASC']] });
  expect(mainList.length).toBe(2);
  expect(mainList[0].userId).toBe('user3');
  expect(mainList[1].userId).toBe('user4');

  const waiting = await Registration.count({ where: { classId: 1, status: 'WAITING' } });
  expect(waiting).toBe(0);
});
