#!/usr/bin/env bash
# One-click start (macOS, Linux, Git Bash): ./start.sh [supabase|local|demo] [port]
#   ./start.sh         Supabase (reads config/supabase.properties)
#   ./start.sh local   in-memory H2, no account needed
#   ./start.sh demo    Supabase + N+1 demo output in logs/app.log
MODE="${1:-supabase}"
PORT="${2:-8080}"
TIMEOUT=180
cd "$(dirname "$0")" || exit 1
ROOT="$(pwd)"
mkdir -p logs

ok()   { printf '\033[32m[ OK ]\033[0m %s\n' "$*"; }
info() { printf '\033[36m[INFO]\033[0m %s\n' "$*"; }
fail() { printf '\033[31m[FAIL]\033[0m %s\n' "$*"; exit 1; }

# ---- 1. Java 21+ ----
JAVA=""
for c in "${JAVA_HOME:+$JAVA_HOME/bin/java}" "$(command -v java)" "$HOME"/.jdks/*/bin/java "$HOME"/.sdkman/candidates/java/current/bin/java; do
  [ -n "$c" ] && [ -x "$c" ] || continue
  major=$("$c" -version 2>&1 | head -n 1 | sed -E 's/.*"([0-9]+).*/\1/')
  if [ "${major:-0}" -ge 21 ] 2>/dev/null; then JAVA="$c"; break; fi
done
[ -n "$JAVA" ] || fail "JDK 21 or newer not found. See prerequisites/README.md"
ok "Java: $("$JAVA" -version 2>&1 | head -n 1)"
export JAVA_HOME="$(cd "$(dirname "$JAVA")/.." && pwd)"

# ---- 2. already running? ----
if [ -f .app.pid ] && kill -0 "$(cat .app.pid)" 2>/dev/null; then
  fail "Already running (PID $(cat .app.pid)). Run ./stop.sh first, or open http://localhost:$PORT"
fi
rm -f .app.pid
if command -v lsof >/dev/null 2>&1 && lsof -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  fail "Port $PORT is already in use. Run ./stop.sh, or choose another port: ./start.sh $MODE 8081"
fi

# ---- 3. database settings check ----
ARGS=("--server.port=$PORT")
if [ "$MODE" = "local" ]; then
  ARGS+=("--spring.profiles.active=local")
  info "Mode: local (in-memory H2, no Supabase needed)"
else
  CFG=config/supabase.properties
  [ -f "$CFG" ] || fail "$CFG not found. Copy config/supabase.properties.example to $CFG and fill in the password."
  prop() { grep -E "^[[:space:]]*$1[[:space:]]*=" "$CFG" | head -n 1 | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//; s/\r$//'; }
  DBHOST=$(prop supabase.host); DBPORT=$(prop supabase.port); DBUSER=$(prop supabase.user); DBPASS=$(prop supabase.password)
  DBPORT="${DBPORT:-5432}"
  [ -n "$DBHOST" ] && [ -n "$DBUSER" ] && [ -n "$DBPASS" ] || fail "supabase.host / user / password must be set in $CFG"
  case "$DBPASS" in *YOUR-DB-PASSWORD*|*YOUR-PASSWORD*) fail "Set supabase.password in $CFG (Supabase > Project Settings > Database > Reset password).";; esac
  info "Mode: Supabase ($DBHOST:$DBPORT, user $DBUSER)"
  if ! (exec 3<>"/dev/tcp/$DBHOST/$DBPORT") 2>/dev/null; then
    fail "Cannot reach $DBHOST:$DBPORT. The direct host is IPv6 only: on an IPv4-only network switch to the Session pooler (see $CFG.example), or run: ./start.sh local"
  fi
  ok "Database host reachable"
  [ "$MODE" = "demo" ] && ARGS+=("--demo.n-plus-one=true")
fi

# ---- 4. build when needed ----
JAR=$(ls target/placement-portal-*.jar 2>/dev/null | grep -v original | head -n 1)
if [ -z "$JAR" ] || [ -n "$(find src pom.xml -newer "$JAR" -type f 2>/dev/null | head -n 1)" ]; then
  info "Building (first run downloads dependencies, this can take a few minutes) ..."
  chmod +x mvnw
  ./mvnw -B -q -DskipTests package > logs/build.log 2>&1 || { tail -n 25 logs/build.log; fail "Build failed, see logs/build.log"; }
  JAR=$(ls target/placement-portal-*.jar | grep -v original | head -n 1)
  ok "Build done"
else
  ok "Jar is up to date"
fi

# ---- 5. start ----
nohup "$JAVA" -jar "$JAR" "${ARGS[@]}" > logs/app.log 2> logs/app.err.log &
echo $! > .app.pid
PID=$(cat .app.pid)
info "Started PID $PID; waiting for health (up to ${TIMEOUT}s) ..."

# ---- 6. health check ----
BASE="http://localhost:$PORT"
healthy=0
for _ in $(seq 1 $((TIMEOUT / 2))); do
  kill -0 "$PID" 2>/dev/null || break
  if curl -fs --max-time 3 "$BASE/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then healthy=1; break; fi
  sleep 2
done
if [ "$healthy" != "1" ]; then
  echo "--- last log lines ---"; tail -n 30 logs/app.log; tail -n 10 logs/app.err.log
  kill "$PID" 2>/dev/null; rm -f .app.pid
  fail "Application did not become healthy. See README.md (Troubleshooting) and logs/app.log"
fi
ok "Health: UP"

# ---- 7. smoke checks ----
bad=0
for p in / /jobs /companies /students /applications /api/jobs; do
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 20 "$BASE$p")
  if [ "$code" = "200" ]; then ok "$(printf '%-14s HTTP %s' "$p" "$code")"; else bad=$((bad+1)); printf '\033[31m[FAIL]\033[0m %-14s HTTP %s\n' "$p" "$code"; fi
done
echo
[ "$bad" = "0" ] && printf '\033[32mPlacement Portal is running (%s mode)\033[0m\n' "$MODE" || echo "Running, but $bad page(s) failed. See logs/app.log"
echo "  App        $BASE"
echo "  Jobs       $BASE/jobs"
echo "  N+1 demo   $BASE/applications?slow=true   (fast: $BASE/applications)"
echo "  JSON       $BASE/api/jobs"
echo "  Health     $BASE/actuator/health"
echo "  Log        $ROOT/logs/app.log"
echo "  Stop       ./stop.sh"
