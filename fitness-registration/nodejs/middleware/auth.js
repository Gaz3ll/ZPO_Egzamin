const basicAuth = require('express-basic-auth');
const crypto = require('crypto');

const hashPassword = (pass) => crypto.createHash('sha256').update(pass).digest('hex');

const HASHED_USERS = {
  user1: hashPassword('pass'),
  user2: hashPassword('pass'),
};

const auth = basicAuth({
  authorizer: (username, password) => HASHED_USERS[username] === hashPassword(password),
  challenge: true,
  unauthorizedResponse: { error: 'Unauthorized' },
});

module.exports = { auth };
