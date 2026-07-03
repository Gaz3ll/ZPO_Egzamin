const basicAuth = require('express-basic-auth');

// Użytkownicy oraz ich hasła w systemie dietetycznym
const USERS = {
  user1: 'pass',
  user2: 'pass',
};

// Mapowanie ról użytkowników
const ROLES = {
  user1: 'USER',
  user2: 'USER',
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
