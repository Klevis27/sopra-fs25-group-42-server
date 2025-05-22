/* ------------------------------------------------------------------------------------------
DEPLOYMENT

First expose port 8080 in Dockerfile and change port 1234 to 8080 in server.js; also set isProduction to true. Then run:

1. cd yjs/
2. gcloud builds submit --tag gcr.io/sopra-fs25-group-42-server/yjs-server:latest .
3. gcloud run deploy yjs-server \
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

// Imports
const http = require('http');
const WebSocket = require('ws');
const {setupWSConnection} = require('y-websocket/bin/utils');
const Y = require('yjs');
const debounce = require('lodash.debounce');
const axios = require('axios');
const {readSyncMessage, messageYjsSyncStep1, messageYjsSyncStep2, writeSyncStep2} = require("y-protocols/sync");
let encoding, decoding;
(async () => {
    const lib0 = await import('lib0');
    encoding = lib0.encoding;
    decoding = lib0.decoding;
    // You can start your server here or call your setup function now


// Production check
    const isProduction = true;
    const API_BASE_URL = isProduction
            ? "https://sopra-fs25-group-42-server.oa.r.appspot.com"
            : 'http://backend:8080';

// Initialisations
    const docs = new Map();
    const pendingSaves = new Map();

// WebSocket and HTTP setup
    const server = http.createServer();
    const wss = new WebSocket.Server({
        noServer: true,
        clientTracking: true,
        keepalive: true,
        keepaliveInterval: 25000
    });

// GetYDoc
    const getYDoc = (docName) => {
        console.log(`[getYDoc] Request for doc "${docName}"`);
        if (!docs.has(docName)) {
            const doc = new Y.Doc();
            docs.set(docName, doc);

            // Observe updates
            doc.on('update', (update, origin) => {
                console.log(`[UPDATE] ${docName} from ${origin || 'remote'} (${update.byteLength} bytes)`);
                debouncedPersist()
            });

            const persist = async () => {
                try {
                    const update = Y.encodeStateAsUpdate(doc);
                    await axios.put(`${API_BASE_URL}/notes/${docName}/state`,
                            Buffer.from(update),
                            { headers: {'Content-Type': 'application/octet-stream'} }
                    );
                    console.log(`Persisted "${docName}"`);
                } catch (err) {
                    console.error(`Persistence error: ${err.message}`);
                }
            };

            const debouncedPersist = debounce(persist, 2000);
            pendingSaves.set(docName, debouncedPersist);

            axios.get(`${API_BASE_URL}/notes/${docName}/state`, {
                responseType: 'arraybuffer',
                validateStatus: (status) => status === 200 || status === 404
            }).then(res => {
                if (res.status === 200 && res.data.byteLength > 0) {
                    try {
                        const update = new Uint8Array(res.data);
                        Y.applyUpdate(doc, update);
                    } catch (err) {
                        console.error(`Corrupt initial state for ${docName} - resetting`);
                        docs.delete(docName);
                        doc.destroy();
                        getYDoc(docName);
                    }
                }
            }).catch(console.error);


            doc.on('destroy', () => {
                debouncedPersist.cancel();
                pendingSaves.delete(docName);
            });
        }
        return docs.get(docName);
    };

// Connection handling
    wss.on('connection', (ws, req) => {
        const docName = req.url.slice(1).split('?')[0];
        console.log(`[CONNECT] ${docName}`);
        const doc = getYDoc(docName);

        setupWSConnection(ws, req, {
            doc,
            gc: true,
        });
        ws.on('message', (message) => {
            console.log(`[MSG] CAME IN"${message}"`)
            try {
                const decoder = decoding.createDecoder(new Uint8Array(message));
                const messageType = decoding.readVarUint(decoder);

                if (messageType === messageYjsSyncStep1 || messageType === messageYjsSyncStep2) {
                    // This reads and applies the update to doc internally
                    readSyncMessage(decoder, messageType, doc, ws);

                    // After update, send sync step 2 if needed
                    const encoderReply = encoding.createEncoder();
                    encoding.writeVarUint(encoderReply, messageYjsSyncStep2);
                    writeSyncStep2(encoderReply, doc);
                    ws.send(encoding.toUint8Array(encoderReply));
                } else {
                    // You can add handlers for awareness or other protocols here
                    console.log(`[WS] Unknown message type: ${messageType}`);
                }
            } catch (e) {
                console.error('[WS ERROR]', e);
            }
        });
        ws.on('error', (err) => {
            console.error(`WS error (${docName}):`, err);
        });

        ws.on('close', () => {
            console.log(`Connection closed: ${docName}`);
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

        console.log('Flushing pending saves...');
        for (const [docName, debouncedFn] of pendingSaves) {
            try {
                debouncedFn.flush(); // Force immediate persistence
                console.log(`Flushed ${docName}`);
            } catch (err) {
                console.error(`Flush failed for ${docName}:`, err);
            }
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
        wss.handleUpgrade(req, socket, head, (ws) => {
            wss.emit('connection', ws, req);
        });
    });

    server.listen(8080, () => {
        console.log('[SERVER] Running on port 8080');
    });

})();