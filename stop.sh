#!/usr/bin/env bash
# One-click stop: ./stop.sh [port]
PORT="${1:-8080}"
cd "$(dirname "$0")" || exit 1
stopped=0

if [ -f .app.pid ]; then
  PID=$(cat .app.pid)
  if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    for _ in 1 2 3 4 5 6 7 8 9 10; do kill -0 "$PID" 2>/dev/null || break; sleep 1; done
    kill -9 "$PID" 2>/dev/null
    echo "[ OK ] Stopped PID $PID"; stopped=1
  fi
  rm -f .app.pid
fi

# fallback: an application of this project still listening on the port (for example started from an IDE)
if command -v lsof >/dev/null 2>&1; then
  P2=$(lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null | head -n 1)
  if [ -n "$P2" ]; then
    if ps -p "$P2" -o command= 2>/dev/null | grep -qi placement; then
      kill "$P2"; echo "[ OK ] Stopped application on port $PORT (PID $P2)"; stopped=1
    else
      echo "[WARN] Port $PORT is used by another program (PID $P2); not touching it."
    fi
  fi
fi
[ "$stopped" = "1" ] || echo "[INFO] Nothing to stop: the application is not running."
