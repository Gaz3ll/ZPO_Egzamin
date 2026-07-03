const { FitnessClass, Registration } = require('../models');

/**
 * Zapisuje użytkownika na zajęcia fitness.
 * Sprawdza pojemność zajęć i decyduje, czy zapisać na listę główną (MAIN) czy rezerwową (WAITING).
 */
async function register(userId, classId) {
  const fitnessClass = await FitnessClass.findByPk(classId);
  if (!fitnessClass) return 'Class not found';

  // Sprawdzenie czy użytkownik już jest zarejestrowany
  const existing = await Registration.findOne({ where: { userId, classId } });
  if (existing) return 'Already registered';

  // Pobranie liczby osób na liście głównej
  const mainCount = await Registration.count({ where: { classId, status: 'MAIN' } });

  if (mainCount < fitnessClass.maxCapacity) {
    // Rejestracja na liście głównej
    await Registration.create({ userId, classId, status: 'MAIN', position: null });
    return 'Registered on main list';
  } else {
    // Rejestracja na liście rezerwowej (pozycja = obecna liczba oczekujących + 1)
    const waitingCount = await Registration.count({ where: { classId, status: 'WAITING' } });
    await Registration.create({ userId, classId, status: 'WAITING', position: waitingCount + 1 });
    return `Registered on waiting list, position ${waitingCount + 1}`;
  }
}

/**
 * Wypisuje użytkownika z zajęć.
 * Jeśli użytkownik był na liście głównej, następuje promocja pierwszej osoby z listy rezerwowej.
 * Jeśli był na liście rezerwowej, pozostałe pozycje w kolejce są aktualizowane.
 */
async function unregister(userId, classId) {
  const reg = await Registration.findOne({ where: { userId, classId } });
  if (!reg) return 'Registration not found';

  const wasMain = reg.status === 'MAIN';
  await reg.destroy();

  if (wasMain) {
    // Promocja osoby z listy rezerwowej
    await promoteFromWaiting(classId);
    return 'Unregistered from main list';
  } else {
    // Przenumerowanie pozycji w kolejce
    await reorderWaiting(classId);
    return 'Unregistered from waiting list';
  }
}

/**
 * Promuje pierwszą osobę z kolejki oczekujących na listę główną.
 */
async function promoteFromWaiting(classId) {
  const waiting = await Registration.findAll({
    where: { classId, status: 'WAITING' },
    order: [['position', 'ASC']],
  });

  if (waiting.length > 0) {
    const promoted = waiting[0];
    promoted.status = 'MAIN';
    promoted.position = null; // Zapis na listę główną usuwa pozycję w kolejce
    await promoted.save();
    // Ponowne uporządkowanie pozostałej listy rezerwowej
    await reorderWaiting(classId);
  }
}

/**
 * Utrzymuje spójność pozycji w kolejce oczekujących (1..N).
 */
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
