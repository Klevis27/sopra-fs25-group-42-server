const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection } = require('y-websocket/bin/utils');
const Redis = require('redis');
const Y = require('yjs');
const { getYDoc } = require('y-websocket/bin/utils');


// Redis setup
const redisClient = Redis.createClient({
  url: process.env.REDIS_URL || 'redis://localhost:6379'
});

redisClient.on('error', (err) => console.error('Redis error:', err));
redisClient.on('connect', () => console.log('✅ Connected to Redis'));

// Create basic HTTP server just to support WebSocket upgrade
const server = http.createServer();
const map = new Map(); // To hold Y.Docs per room name

// WebSocket server on top of HTTP
const wss = new WebSocket.Server({ noServer: true });

wss.on('connection', (ws, req) => {
  console.log('🔌 New client connected:', req.url);
  const docName = req.url.slice(1).split('?')[0];

  console.log("Docname: ", docName);
  const ydoc = getYDoc('your-doc-name');
  //const noteId = ydoc.getMap("meta").get("noteId");
  const noteId = docName.split('/')[0];
  const markdown = ydoc.getText('markdown').toString();
  console.log('Loaded markdown:', markdown);
  console.log("Noteid: ", noteId); 
  setupWSConnection(ws, req, { gc: true });
});

// Upgrade HTTP to WebSocket and pass `req`
server.on('upgrade', (request, socket, head) => {
  wss.handleUpgrade(request, socket, head, (ws) => {
    wss.emit('connection', ws, request);
  });
});

// Start listening (you can change this port or reuse an existing one)
const PORT = 1234;
server.listen(PORT, () => {
  console.log(`🚀 WebSocket server listening on ws://localhost:${PORT}`);
});
