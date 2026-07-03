const basicAuth = require('express-basic-auth');

const USERS = {
  student1: 'pass',
  teacher1: 'pass',
};
const ROLES = {
  student1: 'STUDENT',
  teacher1: 'TEACHER',
};

const auth = basicAuth({
  users: USERS,
  challenge: true,
  unauthorizedResponse: { error: 'Unauthorized' },
});

function requireRole(role) {
  return (req, res, next) => {
    const userRole = ROLES[req.auth.user];
    if (userRole !== role) {
      return res.status(403).json({ error: 'Forbidden' });
    }
    next();
  };
}

module.exports = { auth, requireRole, ROLES };
