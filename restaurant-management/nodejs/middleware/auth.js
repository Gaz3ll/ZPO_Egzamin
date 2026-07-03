const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  guest1: hashPassword('pass'),
  guest2: hashPassword('pass'),
  waiter1: hashPassword('pass'),
};

const ROLES = {
  guest1: 'GUEST',
  guest2: 'GUEST',
  waiter1: 'WAITER',
};

const auth = basicAuth({
  authorizer: (username, password) => HASHED_USERS[username] === hashPassword(password),
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
