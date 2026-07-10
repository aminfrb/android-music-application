const router = require('express').Router();
const db = require('../db');
const { requireAuth } = require('../auth');

const mapSong = (s) => ({
  id: s.id,
  title: s.title,
  artistId: s.artist_id,
  artistName: s.artist_name,
  coverImageUrl: s.cover_image_url,
  audioUrl: s.audio_url,
  durationMs: s.duration_ms,
  genre: s.genre,
  isLocal: !!s.is_local,
  plays: s.plays,
});

/** Everything list-shaped answers with { items, page, hasNext } so Paging 3 can page it. */
function paged(rows, page, size) {
  return { items: rows.map(mapSong), page, hasNext: rows.length === size };
}

router.get('/songs', (req, res) => {
  const page = Number(req.query.page ?? 0);
  const size = Math.min(Number(req.query.size ?? 20), 50);
  const sort = req.query.sort === 'new' ? 'released_at DESC' : 'plays DESC';
  const rows = db.prepare(`SELECT * FROM songs ORDER BY ${sort} LIMIT ? OFFSET ?`)
                 .all(size, page * size);
  res.json(paged(rows, page, size));
});

router.get('/songs/carousel', (_req, res) => {
  const rows = db.prepare(`SELECT * FROM songs ORDER BY released_at DESC LIMIT 8`).all();
  res.json(rows.map(mapSong));
});

router.get('/songs/:id', (req, res) => {
  const s = db.prepare('SELECT * FROM songs WHERE id = ?').get(req.params.id);
  if (!s) return res.status(404).json({ error: 'not_found' });
  res.json(mapSong(s));
});

/** Search across songs and artists. `type` narrows it for the filter chips. */
router.get('/search', (req, res) => {
  const q = `%${(req.query.q || '').trim()}%`;
  const page = Number(req.query.page ?? 0);
  const size = Math.min(Number(req.query.size ?? 20), 50);
  const type = req.query.type || 'all';

  if (type === 'artist') {
    const rows = db.prepare(
      `SELECT * FROM songs WHERE artist_name LIKE ? ORDER BY plays DESC LIMIT ? OFFSET ?`
    ).all(q, size, page * size);
    return res.json(paged(rows, page, size));
  }
  if (type === 'song') {
    const rows = db.prepare(
      `SELECT * FROM songs WHERE title LIKE ? ORDER BY plays DESC LIMIT ? OFFSET ?`
    ).all(q, size, page * size);
    return res.json(paged(rows, page, size));
  }
  const rows = db.prepare(
    `SELECT * FROM songs WHERE title LIKE ? OR artist_name LIKE ? OR genre LIKE ?
     ORDER BY plays DESC LIMIT ? OFFSET ?`
  ).all(q, q, q, size, page * size);
  res.json(paged(rows, page, size));
});

// ---- likes ---------------------------------------------------------------
router.get('/me/likes', requireAuth, (req, res) => {
  const rows = db.prepare(
    `SELECT s.* FROM likes l JOIN songs s ON s.id = l.song_id
     WHERE l.user_id = ? ORDER BY l.liked_at DESC`
  ).all(req.user.id);
  res.json(rows.map(mapSong));
});

router.put('/me/likes/:songId', requireAuth, (req, res) => {
  db.prepare('INSERT OR IGNORE INTO likes (user_id, song_id, liked_at) VALUES (?,?,?)')
    .run(req.user.id, req.params.songId, Date.now());
  res.json({ liked: true });
});

router.delete('/me/likes/:songId', requireAuth, (req, res) => {
  db.prepare('DELETE FROM likes WHERE user_id = ? AND song_id = ?')
    .run(req.user.id, req.params.songId);
  res.json({ liked: false });
});

// ---- recently played -----------------------------------------------------
router.get('/me/recent', requireAuth, (req, res) => {
  const rows = db.prepare(
    `SELECT s.* FROM recently_played r JOIN songs s ON s.id = r.song_id
     WHERE r.user_id = ? ORDER BY r.played_at DESC LIMIT 50`
  ).all(req.user.id);
  res.json(rows.map(mapSong));
});

router.post('/me/recent/:songId', requireAuth, (req, res) => {
  db.prepare(
    `INSERT INTO recently_played (user_id, song_id, played_at) VALUES (?,?,?)
     ON CONFLICT(user_id, song_id) DO UPDATE SET played_at = excluded.played_at`
  ).run(req.user.id, req.params.songId, Date.now());
  db.prepare('UPDATE songs SET plays = plays + 1 WHERE id = ?').run(req.params.songId);
  res.status(204).end();
});

module.exports = router;
module.exports.mapSong = mapSong;
