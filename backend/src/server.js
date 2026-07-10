const express = require('express');
const http = require('http');
const cors = require('cors');
const morgan = require('morgan');
const config = require('./config');
const { attachSockets } = require('./sockets');

const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));
app.use(morgan('dev'));

app.get('/health', (_req, res) => res.json({ ok: true, ts: Date.now() }));

app.use('/api/auth', require('./routes/auth.routes'));
app.use('/api', require('./routes/songs.routes'));
app.use('/api', require('./routes/playlists.routes'));
app.use('/api', require('./routes/social.routes'));
app.use('/api', require('./routes/chat.routes'));

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: 'server_error' });
});

const server = http.createServer(app);
attachSockets(server);

server.listen(config.port, () =>
  console.log(`Nava backend listening on http://0.0.0.0:${config.port}`)
);
