#!/bin/bash

# Collect all host:port pairs until we hit "--"
while [[ "$1" != "--" ]]; do
  if [[ "$1" =~ ^([^:]+):([0-9]+)$ ]]; then
    host="${BASH_REMATCH[1]}"
    port="${BASH_REMATCH[2]}"
    echo "Waiting for $host:$port to be available..."

    while ! (echo > /dev/tcp/$host/$port) 2>/dev/null; do
      echo "Still waiting for $host:$port..."
      sleep 1
    done

    echo "$host:$port is available."
  else
    echo "Invalid host:port format: $1"
    exit 1
  fi
  shift
done

# Shift past the "--"
shift

# Execute the remaining command
exec "$@"
