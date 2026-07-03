const basicAuth = require('express-basic-auth');

// Użytkownicy oraz ich hasła w systemie restauracji
const USERS = {
  guest1: 'pass',
  guest2: 'pass',
  waiter1: 'pass',
};

// Mapowanie ról użytkowników
const ROLES = {
  guest1: 'GUEST',
  guest2: 'GUEST',
  waiter1: 'WAITER',
};

// Middleware Basic Auth
const auth = basicAuth({
  users: USERS,
  challenge: true,
  unauthorizedResponse: { error: 'Unauthorized' },
});

// Funkcyjne sprawdzenie roli użytkownika (brak instrukcji if)
function requireRole(role) {
  return (req, res, next) => {
    const userRole = ROLES[req.auth.user];
    return userRole === role 
      ? next() 
      : res.status(403).json({ error: 'Forbidden' });
  };
}

module.exports = { auth, requireRole, ROLES };
