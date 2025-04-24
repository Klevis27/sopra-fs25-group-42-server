const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection } = require('y-websocket/bin/utils');
const Redis = require('redis');
const Y = require('yjs');
const { getYDoc } = require('y-websocket/bin/utils');
const axios = require('axios');


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

////////////////////////////////////////////////////////////////////////
// Map to store doc metadata
const docMetaMap = new Map();

// Function to periodically save doc content
const autosaveInterval = 10000; // 10 seconds

setInterval(() => {
  for (const [docName, ydoc] of docMetaMap.entries()) {
    try {
      const noteId = ydoc.getMap("meta").get("noteId");
      const markdown = ydoc.getText("markdown").toString();
      const accessToken = ydoc.getMap("meta").get("accessToken");

      axios.get('http://backend:8080/actuator/health')
        .then(res => console.log('✅ Success:', res.data))
        .catch(err => console.error('❌ Failed:', err.message));

      if (noteId && markdown !== undefined) {
        axios.put(`http://backend:8080/noteState/${noteId}`, {
          noteId,
          markdown
        }, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
          }
        }).then(res => {
          console.log(`💾 Autosaved noteId ${noteId}:`, res.status);
        }).catch(err => {
          console.error(`❌ Autosave failed for noteId ${noteId}:`, err.message);
        });
      }
    } catch (err) {
      console.error('💥 Error in autosave:', err);
    }
  }
}, autosaveInterval);
////////////////////////////////////////////////////////////////////////

wss.on('connection', (ws, req) => {
  console.log('🔌 New client connected:', req.url);
  const docName = req.url.slice(1).split('?')[0];

  const ydoc = getYDoc(docName);

  ////////////////////////////////////////////////////////////////////////
  // Track doc for autosave
  docMetaMap.set(docName, ydoc);
  ////////////////////////////////////////////////////////////////////////

  const noteId = ydoc.getMap("meta").get("noteId");

  console.log("Noteid: ", noteId);
  setupWSConnection(ws, req, { gc: true });
});

// Upgrade HTTP to WebSocket and pass `req`
server.on('upgrade', (request, socket, head) => {
  const docName = request.url.slice(1).split('?')[0];
  const ydoc = getYDoc(docName);
  const markdown = ydoc.getText('markdown').toString();
  const noteId = ydoc.getMap("meta").get("noteId");
  const accessToken = ydoc.getMap("meta").get("accessToken");

  console.log("Acces Token: " + accessToken);

  wss.handleUpgrade(request, socket, head, (ws) => {
    wss.emit('connection', ws, request);
  });
});


const PORT = 1234;
server.listen(PORT, () => {
  console.log(`🚀 WebSocket server listening on ws://localhost:${PORT}`);
});
