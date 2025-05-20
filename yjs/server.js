isProduction = true; // needs to be set manually!
const API_BASE_URL = isProduction ? "https://sopra-fs25-group-42-server.oa.r.appspot.com" : 'http://host.docker.internal:8080';
const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection } = require('y-websocket/bin/utils');
const Y = require('yjs');
const debounce = require('lodash.debounce');
const axios = require('axios');

isProduction = true; // Flip to true for production
const API_BASE_URL = isProduction ? "https://sopra-fs25-group-42-server.oa.r.appspot.com" : 'http://backend:8080';

// In-memory docs map
const docs = new Map();

const getYDoc = (docName) => {
    let doc = docs.get(docName);
    if (!doc) {
        doc = new Y.Doc();
        docs.set(docName, doc);

        // Try to fetch persisted state
        axios.get(`${API_BASE_URL}/notes/${docName}/state`, {
            responseType: 'arraybuffer',
        }).then(res => {
            const update = new Uint8Array(res.data);
            Y.applyUpdate(doc, update);
            console.log(`Loaded document "${docName}" from DB`);
        }).catch(err => {
            console.log(`Could not load document "${docName}" from DB. Reason:`, err.message);
        });

        // Debounced persistence
        const persist = debounce(() => {
            const update = Y.encodeStateAsUpdate(doc);
            console.log(`PRE Persisting "${docName}" to DB`);
            axios.put(`${API_BASE_URL}/notes/${docName}/state`, update, {
                headers: {
                    'Content-Type': 'application/octet-stream',
                },
            }).then(() => {
                console.log(`Persisted "${docName}" to DB`);
            }).catch(err => {
                console.error(`Error persisting "${docName}":`, err.message);
            });
        }, 2000);

        doc.on('update', persist);
    }
    return doc;
};

// HTTP & WebSocket server
const server = http.createServer();
const wss = new WebSocket.Server({ noServer: true });

wss.on('connection', (ws, req) => {
    const docName = req.url.slice(1).split('?')[0];
    const doc = getYDoc(docName);
    setupWSConnection(ws, req, { doc });
});

server.on('upgrade', (req, socket, head) => {
    wss.handleUpgrade(req, socket, head, ws => {
        wss.emit('connection', ws, req);
    });
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
    console.log(`WebSocket server listening on ws://0.0.0.0:${PORT}`);
});
