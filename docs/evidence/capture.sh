#!/usr/bin/env bash
# Menangkap output mentah untuk docs/evidence/captures/ dari stack yang berjalan.
# Memakai project dan port terpisah supaya tidak bentrok dengan stack lain:
#   bash docs/evidence/capture.sh          # up + capture (stack dibiarkan menyala)
#   bash docs/evidence/capture.sh down     # docker compose down -v
set -eu

PROJECT="${PROJECT:-docs-capture}"
export GATEWAY_PORT="${GATEWAY_PORT:-18080}"
BASE="http://localhost:${GATEWAY_PORT}"
OUT="$(cd "$(dirname "$0")" && pwd)/captures"
cd "$(dirname "$0")/../.."
dc() { docker compose -p "$PROJECT" "$@"; }

if [ "${1:-}" = "down" ]; then
    dc down -v 2>&1 | tee "$OUT/09-compose-down.txt"
    exit 0
fi

mkdir -p "$OUT"
{ docker --version; docker compose version; git rev-parse --short HEAD; } > "$OUT/00-versions.txt"

start=$(date +%s)
dc up --build --wait > "$OUT/01-compose-up.txt" 2>&1
echo "up --build --wait selesai dalam $(( $(date +%s) - start )) detik" >> "$OUT/01-compose-up.txt"

dc ps > "$OUT/02-compose-ps.txt"
bash scripts/smoke-test.sh "$BASE" > "$OUT/03-smoke-test.txt" 2>&1 || true

curl -s "$BASE/api/users" -o "$OUT/users.json"
curl -s "$BASE/api/posts" -o "$OUT/posts.xml"
curl -s -D - -o /dev/null "$BASE/api/users" > "$OUT/04-users-headers.txt"
curl -s -D - -o /dev/null "$BASE/api/posts" > "$OUT/05-posts-headers.txt"

python - "$OUT" <<'PY' > "$OUT/06-users-summary.txt"
import json, sys
d = json.load(open(sys.argv[1] + "/users.json", encoding="utf-8"))
print("user  :", len(d))
print("todo  :", sum(len(u.get("todos", [])) for u in d))
print("post  :", sum(len(u.get("posts", [])) for u in d))
print("field user pertama :", ", ".join(d[0].keys()))
print("field todo pertama :", ", ".join(d[0]["todos"][0].keys()))
print("field post pertama :", ", ".join(d[0]["posts"][0].keys()))
PY
head -n 2 "$OUT/posts.xml" > "$OUT/07-posts-first-2-lines.txt"
printf 'jumlah <row>: %s\n' "$(grep -o '<row>' "$OUT/posts.xml" | wc -l | tr -d ' ')" >> "$OUT/07-posts-first-2-lines.txt"

sleep 5
docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}' \
    $(dc ps -q) > "$OUT/08-docker-stats.txt"

curl -s "$BASE/eureka/apps" -H 'Accept: application/json' -o "$OUT/10-eureka-apps.json"
echo "Capture selesai. Stack masih menyala di $BASE; jalankan 'bash docs/evidence/capture.sh down' setelah screenshot."
