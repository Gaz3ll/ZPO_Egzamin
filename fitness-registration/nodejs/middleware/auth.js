const basicAuth = require('express-basic-auth');

const USERS = {
  user1: 'pass',
  user2: 'pass',
};

const auth = basicAuth({
  users: USERS,
  challenge: true,
  unauthorizedResponse: { error: 'Unauthorized' },
});

module.exports = { auth };
