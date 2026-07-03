const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  user1: hashPassword('pass'),
  user2: hashPassword('pass'),
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

module.exports = { auth };
