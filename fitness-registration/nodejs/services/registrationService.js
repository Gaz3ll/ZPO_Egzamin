const { FitnessClass, Registration } = require('../models');

async function register(userId, classId) {
  const fitnessClass = await FitnessClass.findByPk(classId);
  if (!fitnessClass) return 'Class not found';

  const existing = await Registration.findOne({ where: { userId, classId } });
  if (existing) return 'Already registered';

  const mainCount = await Registration.count({ where: { classId, status: 'MAIN' } });

  if (mainCount < fitnessClass.maxCapacity) {
    await Registration.create({ userId, classId, status: 'MAIN', position: null });
    return 'Registered on main list';
  } else {
    const waitingCount = await Registration.count({ where: { classId, status: 'WAITING' } });
    await Registration.create({ userId, classId, status: 'WAITING', position: waitingCount + 1 });
    return `Registered on waiting list, position ${waitingCount + 1}`;
  }
}

async function unregister(userId, classId) {
  const reg = await Registration.findOne({ where: { userId, classId } });
  if (!reg) return 'Registration not found';

  const wasMain = reg.status === 'MAIN';
  await reg.destroy();

  if (wasMain) {
    await promoteFromWaiting(classId);
    return 'Unregistered from main list';
  } else {
    await reorderWaiting(classId);
    return 'Unregistered from waiting list';
  }
}

async function promoteFromWaiting(classId) {
  const waiting = await Registration.findAll({
    where: { classId, status: 'WAITING' },
    order: [['position', 'ASC']],
  });

  if (waiting.length > 0) {
    const promoted = waiting[0];
    promoted.status = 'MAIN';
    promoted.position = null;
    await promoted.save();
    await reorderWaiting(classId);
  }
}

async function reorderWaiting(classId) {
  const waiting = await Registration.findAll({
    where: { classId, status: 'WAITING' },
    order: [['position', 'ASC']],
  });

  for (let i = 0; i < waiting.length; i++) {
    waiting[i].position = i + 1;
    await waiting[i].save();
  }
}

module.exports = { register, unregister };
