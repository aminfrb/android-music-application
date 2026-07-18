/**
 * Seeds the database with 50 real, freely-streamable songs.
 *
 * Audio: SoundHelix royalty-free demo tracks (https://www.soundhelix.com) --
 *        stable, CORS-friendly MP3s that ExoPlayer streams without a key.
 * Covers: picsum.photos deterministic seeds.
 *
 * Swap AUDIO_POOL for your own CDN / Firebase Storage links whenever you like;
 * nothing else in the project depends on where the bytes come from.
 */
const db = require('../db');
const bcrypt = require('bcryptjs');

const now = () => Date.now();

const AUDIO_POOL = Array.from({ length: 16 }, (_, i) =>
  `https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${i + 1}.mp3`
);

const WORLD_ARTISTS = [
  'Aurora Fields', 'Neon Harbor', 'Kite & Bloom', 'The Slow Tide',
  'Marisol Vane', 'Paper Lantern', 'Odd Compass', 'Velvet Static',
];
const LOCAL_ARTISTS = [
  'شهرزاد', 'کوچه ابری', 'مهتاب مهر', 'نوید آرام',
  'باران‌نواز', 'ماهور', 'سیمرغ', 'دشت روشن',
];

const WORLD_TITLES = [
  'Glass Morning', 'Undertow', 'Paper Skies', 'Long Way North', 'Sable',
  'Halfway Lights', 'Cinder', 'Blue Hour', 'Static Bloom', 'Low Orbit',
  'Tin Roof', 'Ember Season', 'Salt & Signal', 'Northbound', 'Quiet Machine',
  'Golden Ratio', 'Driftwood', 'Slow Comet', 'Pale Fire', 'Analog Heart',
  'Riverbed', 'Nightshade', 'Copper Rain', 'Foxglove', 'Wide Awake',
];
const LOCAL_TITLES = [
  'کوچه بی‌انتها', 'باران که می‌بارد', 'شب‌های تهران', 'مثل همیشه', 'دلتنگی',
  'سفر', 'ماه بی‌تاب', 'حوالی صبح', 'خاک نم‌خورده', 'پنجره',
  'یادگاری', 'آواز باد', 'ردپا', 'چای دارچین', 'برف نیامده',
  'ساعت پنج', 'دو قدم مانده', 'گنجشک‌ها', 'نامه', 'کوچ',
  'صبح جمعه', 'زمستان کوتاه', 'رود', 'سایه‌روشن', 'هم‌قدم',
];

const GENRES = ['Chill', 'Electronic', 'Acoustic', 'Lo-fi', 'Ambient', 'Pop', 'Classical'];
const cover = (seed) => `https://picsum.photos/seed/ava${seed}/600/600`;

function reset() {
  const tables = ['messages','conversations','artist_follows','follows','recently_played',
    'likes','playlist_songs','playlists','songs','artists','users'];
  tables.forEach((t) => db.prepare(`DELETE FROM ${t}`).run());
  db.prepare(`DELETE FROM sqlite_sequence`).run();
}

function seed() {
  reset();

  // ---- users -------------------------------------------------------------
  const insertUser = db.prepare(
    `INSERT INTO users (username, display_name, password_hash, avatar_url, is_premium, created_at)
     VALUES (?,?,?,?,?,?)`
  );
  const hash = bcrypt.hashSync('123456', 8);
  const demoUsers = [
    ['rezayee', 'رضایی', 1], ['mohammadi', 'محمدی', 0], ['karimi', 'کریمی', 0],
    ['ahmadi', 'احمدی', 1], ['naseri', 'ناصری', 0],
  ];
  demoUsers.forEach(([u, d, p], i) =>
    insertUser.run(u, d, hash, `https://i.pravatar.cc/300?img=${i + 11}`, p, now())
  );

  // ---- artists -----------------------------------------------------------
  const insertArtist = db.prepare(
    `INSERT INTO artists (name, image_url, followers) VALUES (?,?,?)`
  );
  const artistIds = {};
  [...WORLD_ARTISTS, ...LOCAL_ARTISTS].forEach((name, i) => {
    const info = insertArtist.run(name, cover(`artist${i}`), 1000 + i * 137);
    artistIds[name] = info.lastInsertRowid;
  });

  // ---- 50 songs ----------------------------------------------------------
  const insertSong = db.prepare(
    `INSERT INTO songs (title, artist_id, artist_name, cover_image_url, audio_url,
                        duration_ms, genre, is_local, plays, released_at)
     VALUES (@title,@artist_id,@artist_name,@cover,@audio,@dur,@genre,@is_local,@plays,@released)`
  );

  const rows = [];
  WORLD_TITLES.forEach((title, i) => {
    const artist = WORLD_ARTISTS[i % WORLD_ARTISTS.length];
    rows.push({
      title, artist_id: artistIds[artist], artist_name: artist,
      cover: cover(`w${i}`), audio: AUDIO_POOL[i % AUDIO_POOL.length],
      dur: 180000 + (i % 7) * 23000, genre: GENRES[i % GENRES.length],
      is_local: 0, plays: 5000 - i * 91,
      released: now() - i * 86400000,
    });
  });
  LOCAL_TITLES.forEach((title, i) => {
    const artist = LOCAL_ARTISTS[i % LOCAL_ARTISTS.length];
    rows.push({
      title, artist_id: artistIds[artist], artist_name: artist,
      cover: cover(`l${i}`), audio: AUDIO_POOL[(i + 5) % AUDIO_POOL.length],
      dur: 175000 + (i % 5) * 31000, genre: GENRES[(i + 3) % GENRES.length],
      is_local: 1, plays: 4200 - i * 77,
      released: now() - (i + 3) * 86400000,
    });
  });
  const insertMany = db.transaction((list) => list.forEach((r) => insertSong.run(r)));
  insertMany(rows);

  // ---- playlists ---------------------------------------------------------
  const insertPl = db.prepare(
    `INSERT INTO playlists (title, cover_url, owner_id, is_public, kind, created_at)
     VALUES (?,?,?,?,?,?)`
  );
  const insertPlSong = db.prepare(
    `INSERT OR IGNORE INTO playlist_songs (playlist_id, song_id, position) VALUES (?,?,?)`
  );

  const world = ['Late Night Drive', 'Focus Deep', 'Morning Coffee', 'Rainy Window'];
  const local = ['بهترین‌های پاپ ایرانی', 'شب‌های پاییزی', 'سنتی و آرام', 'کافه‌نشینی'];
  const mine  = ['پلی‌لیست من', 'برای دویدن'];

  const addPl = (title, kind, owner, songIds, seedKey) => {
    const id = insertPl.run(title, cover(seedKey), owner, 1, kind, now()).lastInsertRowid;
    songIds.forEach((sid, idx) => insertPlSong.run(id, sid, idx));
  };

  const worldSongIds = db.prepare(`SELECT id FROM songs WHERE is_local=0`).all().map(r => r.id);
  const localSongIds = db.prepare(`SELECT id FROM songs WHERE is_local=1`).all().map(r => r.id);

  world.forEach((t, i) => addPl(t, 'world', null, worldSongIds.slice(i * 4, i * 4 + 8), `pw${i}`));
  local.forEach((t, i) => addPl(t, 'local', null, localSongIds.slice(i * 4, i * 4 + 8), `pl${i}`));
  mine.forEach((t, i) => addPl(t, 'user', 1, [...worldSongIds, ...localSongIds].slice(i * 6, i * 6 + 6), `pu${i}`));

  // ---- social graph ------------------------------------------------------
  const follow = db.prepare(`INSERT OR IGNORE INTO follows (follower_id, followee_id) VALUES (?,?)`);
  follow.run(1, 2); follow.run(1, 3); follow.run(2, 1); follow.run(3, 1); follow.run(4, 1);

  console.log(`Seeded: ${rows.length} songs, ${WORLD_ARTISTS.length + LOCAL_ARTISTS.length} artists, ${demoUsers.length} users.`);
  console.log('Demo login -> username: ali  password: 123456  (premium)');
}

seed();
