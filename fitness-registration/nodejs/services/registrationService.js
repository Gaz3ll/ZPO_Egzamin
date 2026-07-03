const { FitnessClass, Registration } = require('../models');

/**
 * Zapisuje użytkownika na zajęcia fitness (podejście funkcyjne).
 */
async function register(userId, classId) {
  const fitnessClass = await FitnessClass.findByPk(classId);
  const existing = await Registration.findOne({ where: { userId, classId } });
  const mainCount = await Registration.count({ where: { classId, status: 'MAIN' } });
  const waitingCount = await Registration.count({ where: { classId, status: 'WAITING' } });

  // Brak if-ów - używamy wyrażenia warunkowego trójargumentowego do walidacji i rejestracji
  return !fitnessClass 
    ? 'Class not found'
    : existing 
      ? 'Already registered'
      : mainCount < fitnessClass.maxCapacity 
        ? (await Registration.create({ userId, classId, status: 'MAIN', position: null }), 'Registered on main list')
        : (await Registration.create({ userId, classId, status: 'WAITING', position: waitingCount + 1 }), `Registered on waiting list, position ${waitingCount + 1}`);
}

/**
 * Wypisuje użytkownika z zajęć (podejście funkcyjne).
 */
async function unregister(userId, classId) {
  const reg = await Registration.findOne({ where: { userId, classId } });
  
  // Brak if-ów - używamy zagnieżdżonego wyrażenia trójargumentowego z operatorem przecinkowym dla operacji bazodanowych
  return !reg 
    ? 'Registration not found'
    : (await reg.destroy(), 
       reg.status === 'MAIN' 
         ? (await promoteFromWaiting(classId), 'Unregistered from main list')
         : (await reorderWaiting(classId), 'Unregistered from waiting list'));
}

/**
 * Promuje pierwszą osobę z kolejki oczekujących (podejście funkcyjne).
 */
async function promoteFromWaiting(classId) {
  const waiting = await Registration.findAll({
    where: { classId, status: 'WAITING' },
    order: [['position', 'ASC']],
  });

  const promote = async (promoted) => {
    promoted.status = 'MAIN';
    promoted.position = null;
    await promoted.save();
    await reorderWaiting(classId);
  };

  // Użycie operatora logicznego && zamiast tradycyjnego if
  waiting.length > 0 && await promote(waiting[0]);
}

/**
 * Przenumerowuje pozycje w kolejce rezerwowej (podejście funkcyjne bez pętli for).
 */
async function reorderWaiting(classId) {
  const waiting = await Registration.findAll({
    where: { classId, status: 'WAITING' },
    order: [['position', 'ASC']],
  });

  // Brak pętli - używamy map i Promise.all dla asynchronicznego zapisu w stylu funkcyjnym
  await Promise.all(waiting.map((reg, index) => {
    reg.position = index + 1;
    return reg.save();
  }));
}

module.exports = { register, unregister };
