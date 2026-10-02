#!/usr/bin/env bash
# Checks the tools needed for the Placement Portal. Run: ./prerequisites/check-prereqs.sh
ok=1
report() { if [ "$2" = "1" ]; then printf '[ OK ] %-18s %s\n' "$1" "$3"; else printf '[FAIL] %-18s %s\n' "$1" "$3"; ok=0; fi; }

if command -v java >/dev/null 2>&1; then
  line=$(java -version 2>&1 | head -n 1)
  major=$(echo "$line" | sed -E 's/.*"([0-9]+).*/\1/')
  if [ "${major:-0}" -ge 21 ] 2>/dev/null; then report "JDK 21+" 1 "$line"; else report "JDK 21+" 0 "$line (need 21 or newer)"; fi
else
  report "JDK 21+" 0 "java not found; see prerequisites/README.md"
fi

if command -v git >/dev/null 2>&1; then report "Git" 1 "$(git --version)"; else report "Git" 0 "not found"; fi

root="$(cd "$(dirname "$0")/.." && pwd)"
if [ -f "$root/mvnw" ]; then report "Maven wrapper" 1 "mvnw in repo root"; else report "Maven wrapper" 0 "mvnw missing"; fi

if [ -f "$root/config/supabase.properties" ]; then
  printf '[ OK ] %-18s found\n' "supabase.properties"
else
  printf '[INFO] %-18s missing: copy config/supabase.properties.example, or run ./start.sh local (H2)\n' "supabase.properties"
fi

if (command -v lsof >/dev/null 2>&1 && lsof -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1); then
  report "Port 8080 free" 0 "in use; stop the other app or use --server.port=8081"
else
  report "Port 8080 free" 1 "free"
fi

[ "$ok" = "1" ] && echo "All required checks passed." || { echo "Fix the FAIL lines above."; exit 1; }
