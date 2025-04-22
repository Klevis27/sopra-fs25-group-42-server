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
