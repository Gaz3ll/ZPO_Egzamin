const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  user1: hashPassword('pass'),
  user2: hashPassword('pass'),
};

const ROLES = {
  user1: 'USER',
  user2: 'USER',
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
