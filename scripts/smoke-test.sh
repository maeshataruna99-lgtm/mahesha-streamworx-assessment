#!/usr/bin/env sh
# Checks both endpoints through the gateway after `docker compose up`.
# Usage: scripts/smoke-test.sh [base-url]   (default http://localhost:8080)
set -eu

BASE_URL="${1:-http://localhost:8080}"
WAIT_SECONDS="${WAIT_SECONDS:-120}"
failures=0

pass() { printf 'PASS  %s\n' "$1"; }
fail() { printf 'FAIL  %s\n' "$1"; failures=$((failures + 1)); }

# Without `docker compose up --wait` the services may still be registering with Eureka.
waited=0
until [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/users")" = 200 ] \
    && [ "$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/posts")" = 200 ]; do
    if [ "$waited" -ge "$WAIT_SECONDS" ]; then
        echo "Gateway at $BASE_URL did not serve both endpoints within ${WAIT_SECONDS}s"
        exit 1
    fi
    sleep 2
    waited=$((waited + 2))
done

users=$(curl -s -H 'Accept: application/json' "$BASE_URL/api/users")
user_count=$(printf '%s' "$users" | grep -o '"username"' | wc -l | tr -d ' ')
todo_count=$(printf '%s' "$users" | grep -o '"completed"' | wc -l | tr -d ' ')
post_count=$(printf '%s' "$users" | grep -o '"body"' | wc -l | tr -d ' ')

[ "$user_count" = 10 ] && pass "/api/users returns 10 users" || fail "/api/users returned $user_count users"
[ "$post_count" = 100 ] && pass "/api/users includes 100 posts" || fail "/api/users included $post_count posts"
if [ "$todo_count" = 200 ]; then
    pass "/api/users includes 200 todos"
else
    # Todos come from jsonplaceholder.typicode.com; without internet access users are returned without them.
    printf 'WARN  /api/users included %s todos (is jsonplaceholder.typicode.com reachable?)\n' "$todo_count"
fi
printf '%s' "$users" | grep -q '"geo":{"lat":"-37.3159","lng":"81.1496"}' \
    && pass "/api/users keeps nested address.geo" || fail "/api/users is missing address.geo"

posts=$(curl -s "$BASE_URL/api/posts")
row_count=$(printf '%s' "$posts" | grep -o '<row>' | wc -l | tr -d ' ')
[ "$row_count" = 100 ] && pass "/api/posts returns 100 rows" || fail "/api/posts returned $row_count rows"
printf '%s' "$posts" | grep -q '^<?xml' && pass "/api/posts starts with an XML declaration" \
    || fail "/api/posts has no XML declaration"
printf '%s' "$posts" | grep -q '<root><row><userId>1</userId><id>1</id><title>' \
    && pass "/api/posts uses <root>/<row> with userId, id, title" || fail "/api/posts has an unexpected structure"

[ "$failures" -eq 0 ] && echo "All checks passed" || { echo "$failures check(s) failed"; exit 1; }
