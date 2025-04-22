#!/bin/bash

host="$1"
port="$2"
shift 2

echo "Waiting for $host:$port to be available..."

while ! (echo > /dev/tcp/$host/$port) 2>/dev/null; do
  echo "Waiting for $host:$port to be available..."
  sleep 1
done

echo "$host:$port is available. Starting application..."
exec "$@"
