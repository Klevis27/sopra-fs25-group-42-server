const WebSocket = require('ws');
const Y = require('yjs');
const { RedisPersistence } = require('y-redis');
const Redis = require('ioredis');
const { PrismaClient } = require('@prisma/client');
const { WebsocketProvider } = require('y-websocket');

// Initialize dependencies
const prisma = new PrismaClient();
const redis = new Redis({
  host: 'localhost',
  port: 6379
});

// Configure Y-Redis persistence
const persistence = new RedisPersistence({
  redis,
  prefix: 'yjs-docs'
});

// Create WebSocket server
const wss = new WebSocket.Server({ port: 1234 });

// Document cache
const docs = new Map();

// Debounce function for database saves
function debounce(func, timeout = 2000) {
  let timer;
  const debounced = function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => func.apply(this, args), timeout);
  };
  debounced.flush = () => {
    clearTimeout(timer);
    func();
  };
  return debounced;
}

async function loadDocument(docId) {
  // Try to load from database
  const docData = await prisma.document.findUnique({
    where: { id: docId }
  });

  // Get or create Yjs document
  const ydoc = await persistence.getYDoc(docId);
  
  // Initialize from database if empty
  if (docData && ydoc.getText('content').length === 0) {
    ydoc.getText('content').insert(0, docData.content);
  }
  
  return ydoc;
}

wss.on('connection', async (ws, req) => {
  try {
    const urlParams = new URLSearchParams(req.url.split('?')[1]);
    const docId = urlParams.get('docId');
    
    if (!docId) {
      ws.close(4000, 'Missing document ID');
      return;
    }

    // Load or create document
    const ydoc = await loadDocument(docId);
    docs.set(docId, ydoc);

    // Setup WebSocket provider
    const provider = new WebsocketProvider(
      ws,
      docId,
      ydoc,
      { disableBc: true }
    );

    // Database save handler
    const saveToDB = debounce(async () => {
      try {
        const content = ydoc.getText('content').toString();
        await prisma.document.upsert({
          where: { id: docId },
          update: { content },
          create: { id: docId, content }
        });
      } catch (error) {
        console.error('Database save error:', error);
      }
    });

    // Save on document updates
    ydoc.on('update', saveToDB);

    // Cleanup on disconnect
    ws.on('close', () => {
      ydoc.off('update', saveToDB);
      saveToDB.flush();
      provider.destroy();
      if (ydoc._wsClients.size === 0) {
        docs.delete(docId);
      }
    });

  } catch (error) {
    console.error('Connection error:', error);
    ws.close(4002, 'Server error');
  }
});

// Error handling
redis.on('error', (error) => {
  console.error('Redis error:', error);
});

wss.on('error', (error) => {
  console.error('WebSocket error:', error);
});

console.log('Server running on ws://localhost:1234');



// yjs-redis-server/server.js
import Redis from "ioredis";
import * as Y from "yjs";
import fs from "fs";

const CHANNEL = process.env.CHANNEL || "markdown-room";
const REDIS_HOST = process.env.REDIS_HOST || "localhost";
const REDIS_PORT = parseInt(process.env.REDIS_PORT || "6379", 10);

// 1) Our in‑memory Y.Doc
const ydoc = new Y.Doc();

// 2) Two Redis clients
const sub = new Redis({ host: REDIS_HOST, port: REDIS_PORT });
const pub = new Redis({ host: REDIS_HOST, port: REDIS_PORT });

// 3) Publish updates from any peer into Redis
ydoc.on("update", (update) => {
    pub.publish(CHANNEL, update);
});

// 4) When we see an update on the channel, apply it locally
sub.subscribe(CHANNEL, () => {
    console.log(`Subscribed to ${CHANNEL}`);
});
sub.on("messageBuffer", (_channel, message) => {
    Y.applyUpdate(ydoc, new Uint8Array(message));
});

// 5) (Optional) persist a snapshot every minute
setInterval(() => {
    const snapshot = Y.encodeStateAsUpdate(ydoc);
    fs.writeFileSync("./snapshot.bin", snapshot);
    console.log("Snapshot saved");
}, 60_000);

console.log("Y‑Redis bridge up and running");
