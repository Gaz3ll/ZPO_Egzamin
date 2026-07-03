const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

// Haszowanie hasła SHA-256
const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  guest1: hashPassword('pass'),
  guest2: hashPassword('pass'),
  waiter1: hashPassword('pass'),
  waiter2: hashPassword('pass'),
};

const ROLES = {
  guest1: 'GUEST',
  guest2: 'GUEST',
  waiter1: 'WAITER',
  waiter2: 'WAITER',
};

// Porównanie w czasie stałym – zapobiega timing attacks i błędom biblioteki
const safeCompare = (a, b) => {
  const bufA = Buffer.from(a, 'hex');
  const bufB = Buffer.from(b, 'hex');
  return bufA.length === bufB.length && crypto.timingSafeEqual(bufA, bufB);
};

const auth = basicAuth({
  authorizer: (username, password, cb) => {
    const expectedHash = HASHED_USERS[username];
    const actualHash = hashPassword(password);
    return expectedHash
      ? cb(null, safeCompare(expectedHash, actualHash))
      : cb(null, false);
  },
  authorizeAsync: true,
  challenge: true,
  unauthorizedResponse: { error: 'Unauthorized' },
});

function requireRole(role) {
  return (req, res, next) => {
    const userRole = ROLES[req.auth.user];
    return userRole === role
      ? next()
      : res.status(403).json({ error: 'Forbidden' });
  };
}

module.exports = { auth, requireRole, ROLES };
