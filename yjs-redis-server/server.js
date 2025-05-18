const redisConfig = {
    host: process.env.REDIS_HOST || 'redis',
    port: Number(process.env.REDIS_PORT) || 6379,
    password: process.env.REDIS_PASSWORD || 'devpass',
    family: 4,
    enableReadyCheck: true,
    maxRetriesPerRequest: 5,
    lazyConnect: true,
    retryStrategy: null,
};
console.log('Redis config at startup:', redisConfig);

// 2) Monkey‐patch every connect() to override localhost fallbacks
const Redis = require('ioredis');
const originalConnect = Redis.prototype.connect;
Redis.prototype.connect = function connectOverride() {
    // If someone tried to connect to localhost/127.0.0.1, override it
    const h = this.options.host;
    if (!h || h === '127.0.0.1' || h === 'localhost') {
        console.log(
                '🚨 [ioredis] overriding bad host:',
                h,
                '→',
                redisConfig.host + ':' + redisConfig.port
        );
        // blow away any bad defaults
        this.options = { ...this.options, ...redisConfig };
    }
    return originalConnect.apply(this, arguments);
};

// 3) Attach a safe error handler to every new instance
const OriginalCtor = Redis.prototype.constructor;
Redis.prototype.constructor = function (...args) {
    const inst = new OriginalCtor(...args);
    inst.on('error', err =>
            console.error('[ioredis][instance] caught error on', inst.options, err)
    );
    return inst;
};

const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection, getYDoc } = require('y-websocket/bin/utils');
const { RedisPersistence } = require('y-redis');

// 4) Create your primary Redis clients
const mainClient = new Redis(redisConfig).on('ready', () =>
        console.log('[mainClient] Connected!')
);
mainClient.ping().then(() => console.log('[mainClient] PONG'));

// pub/sub for Y-Redis
const pubClient = new Redis(redisConfig);
const subClient = new Redis(redisConfig);

// 5) Hand _all_ Redis clients into your persistence layer
const persistence = new RedisPersistence({
    pubClient,
    subClient,
    // also override the default single-client internally
    redis: mainClient,
    flushEvery: null,
});

// 6) HTTP & WebSocket server
const server = http.createServer();
const wss = new WebSocket.Server({ noServer: true });

wss.on('connection', (ws, req) => {
    console.log('New WS client:', req.url);
    const docName = req.url.slice(1).split('?')[0];
    const ydoc = getYDoc(docName);
    console.log('NoteId:', ydoc.getMap('meta').get('noteId'));

    setupWSConnection(ws, req, { gc: true, persistence });
});

server.on('upgrade', (req, sock, head) => {
    wss.handleUpgrade(req, sock, head, ws => {
        wss.emit('connection', ws, req);
    });
});

const PORT = process.env.PORT || 1234;
server.listen(PORT, () =>
        console.log(`🚀 WebSocket server listening on ws://0.0.0.0:${PORT}`)
);
