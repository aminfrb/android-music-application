const router = require('express').Router();
const bcrypt = require('bcryptjs');
const db = require('../db');
const { sign, requireAuth } = require('../auth');

const publicUser = (u) => ({
  id: u.id,
  username: u.username,
  displayName: u.display_name,
  avatarUrl: u.avatar_url,
  isPremium: !!u.is_premium,
});

router.post('/register', (req, res) => {
  const { username, password, displayName } = req.body || {};
  if (!username || !password) return res.status(400).json({ error: 'username_and_password_required' });

  const exists = db.prepare('SELECT 1 FROM users WHERE username = ?').get(username);
  if (exists) return res.status(409).json({ error: 'username_taken' });

  const info = db.prepare(
    `INSERT INTO users (username, display_name, password_hash, avatar_url, is_premium, created_at)
     VALUES (?,?,?,?,0,?)`
  ).run(username, displayName || username, bcrypt.hashSync(password, 8),
        `https://i.pravatar.cc/300?u=${username}`, Date.now());

  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(info.lastInsertRowid);
  res.status(201).json({ token: sign(user), user: publicUser(user) });
});

router.post('/login', (req, res) => {
  const { username, password } = req.body || {};
  const user = db.prepare('SELECT * FROM users WHERE username = ?').get(username || '');
  if (!user || !bcrypt.compareSync(password || '', user.password_hash)) {
    return res.status(401).json({ error: 'bad_credentials' });
  }
  res.json({ token: sign(user), user: publicUser(user) });
});

router.get('/me', requireAuth, (req, res) => {
  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  res.json(publicUser(user));
});

/** Mock purchase flow: flips the premium flag. No payment provider involved. */
router.post('/me/premium', requireAuth, (req, res) => {
  db.prepare('UPDATE users SET is_premium = 1 WHERE id = ?').run(req.user.id);
  const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id);
  res.json(publicUser(user));
});

router.patch('/me', requireAuth, (req, res) => {
  const { displayName, avatarUrl } = req.body || {};
  db.prepare(`UPDATE users SET display_name = COALESCE(?, display_name),
              avatar_url = COALESCE(?, avatar_url) WHERE id = ?`)
    .run(displayName ?? null, avatarUrl ?? null, req.user.id);
  res.json(publicUser(db.prepare('SELECT * FROM users WHERE id = ?').get(req.user.id)));
});

module.exports = router;
module.exports.publicUser = publicUser;
