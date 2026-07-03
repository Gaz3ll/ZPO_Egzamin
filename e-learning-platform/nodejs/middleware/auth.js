const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  student1: hashPassword('pass'),
  teacher1: hashPassword('pass'),
};

const ROLES = {
  student1: 'STUDENT',
  teacher1: 'TEACHER',
};

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
