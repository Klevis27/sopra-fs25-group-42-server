/* ------------------------------------------------------------------------------------------
DEPLOYMENT

First expose port 8080 in Dockerfile and change port 1234 to 8080 in server.js; also set isProduction to true. Then run:

1. cd yjs/
2. docker build --no-cache -t gcr.io/sopra-fs25-group-42-server/yjs-server:latest .
3. gcloud builds submit --tag gcr.io/sopra-fs25-group-42-server/yjs-server:latest .
4. gcloud run deploy yjs-server \
  --image gcr.io/sopra-fs25-group-42-server/yjs-server:latest \
  --platform managed \
  --region europe-west6 \
  --allow-unauthenticated \
  --memory 1Gi \
  --concurrency 50 \
  --timeout 3600 \
  --min-instances 1 \
  --max-instances 3
------------------------------------------------------------------------------------------ */

const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection } = require('y-websocket/bin/utils');
const Y = require('yjs');
const debounce = require('lodash.debounce');
const axios = require('axios');

const isProduction = true;
const API_BASE_URL = isProduction
        ? "https://sopra-fs25-group-42-server.oa.r.appspot.com"
        : 'http://backend:8080';

// In-memory docs and persistence tracking
const docs = new Map();
const pendingSaves = new Map();

const { GoogleAuth } = require('google-auth-library');
const auth = new GoogleAuth();

// Add this before your Axios calls
async function getAuthHeaders() {
    const client = await auth.getIdTokenClient(
            'https://sopra-fs25-group-42-server.oa.r.appspot.com'
    );
    return client.getRequestHeaders();
}

const getYDoc = (docName) => {
    let doc = docs.get(docName);
    if (!doc) {
        doc = new Y.Doc();
        docs.set(docName, doc);

        // Load initial state
        axios.get(`${API_BASE_URL}/notes/${docName}/state`, {
            responseType: 'arraybuffer',
            validateStatus: (status) => status === 200 || status === 404
        }).then(res => {
            if (res.status === 200 && res.data.byteLength > 0) {
                try {
                    Y.applyUpdate(doc, new Uint8Array(res.data));
                    console.log(`Loaded initial state for "${docName}"`);
                } catch (e) {
                    console.error(`Corrupted data for "${docName}":`, e.message);
                }
            }
        }).catch(console.error);

        // Configure persistence
        const persist = debounce(async () => {
            try {
                const update = Y.encodeStateAsUpdate(doc);
                const headers = {
                    ...await getAuthHeaders(),
                    'Content-Type': 'application/octet-stream'
                };
                await axios.put(`${API_BASE_URL}/notes/${docName}/state`, update, { headers });
                console.log(`Persisted "${docName}" successfully`);
            } catch (err) {
                console.error(`Persistence failed for "${docName}":`, err.message);
            }
        }, 500);

        // Track pending saves and ensure final flush
        doc.on('update', () => {
            // NEVER ENTERS HERE???
            console.log(`doc update triggered for "${docName}", origin:`, origin);
            persist();
            pendingSaves.set(docName, persist);
        });
    }
    return doc;
};

// WebSocket and HTTP setup
const server = http.createServer();
const wss = new WebSocket.Server({
    noServer: true,
    clientTracking: true,
    keepalive: true,
    keepaliveInterval: 25000
});

// Connection handling
wss.on('connection', (ws, req) => {
    const docName = req.url.slice(1).split('?')[0];
    console.log(`New connection: ${docName} (${wss.clients.size} connections)`);

    const doc = getYDoc(docName);
    setupWSConnection(ws, req, { doc });

    ws.on('close', () => {
        console.log(`Connection closed: ${docName} (${wss.clients.size} remaining)`);
    });
});

// Graceful shutdown
process.on('SIGTERM', async () => {
    console.log('Starting graceful shutdown...');

    // 1. Close WebSocket connections
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.close(1001, 'Server maintenance');
        }
    });

    // 2. Flush all pending saves
    console.log(`Flushing ${pendingSaves.size} pending saves...`);
    for (const [docName, persist] of pendingSaves) {
        if (persist.flush) persist.flush();
        console.log(`Flushed pending saves for ${docName}`);
    }

    // 3. Close HTTP server
    await new Promise(resolve => server.close(resolve));
    console.log('Shutdown complete');
    process.exit(0);
});

// Health checks
server.on('request', (req, res) => {
    if (req.url === '/health') {
        res.writeHead(200, {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        });
        res.end(JSON.stringify({
            status: 'OK',
            documents: docs.size,
            connections: wss.clients.size
        }));
        return;
    }
    res.writeHead(404).end();
});

server.on('upgrade', (req, socket, head) => {
    wss.handleUpgrade(req, socket, head, ws => {
        wss.emit('connection', ws, req);
    });
});

server.listen(process.env.PORT || 8080, () => {
    console.log(`Server ready on port ${process.env.PORT || 8080}`);
});
