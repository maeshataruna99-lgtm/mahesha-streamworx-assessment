#!/usr/bin/env sh
# Checks both endpoints through the gateway after `docker compose up`.
# Usage: scripts/smoke-test.sh [base-url]   (default http://localhost:8080)
set -eu

BASE_URL="${1:-http://localhost:8080}"
WAIT_SECONDS="${WAIT_SECONDS:-120}"
failures=0

expect_count() {
    if [ "$2" = "$3" ]; then
        printf 'PASS  %s: %s\n' "$1" "$3"
    else
        printf 'FAIL  %s: expected %s, got %s\n' "$1" "$3" "$2"
        failures=$((failures + 1))
    fi
}

expect_match() {
    if printf '%s' "$2" | grep -q "$3"; then
        printf 'PASS  %s\n' "$1"
    else
        printf 'FAIL  %s\n' "$1"
        failures=$((failures + 1))
    fi
}

count() {
    printf '%s' "$1" | grep -o "$2" | wc -l | tr -d ' '
}

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

users=$(curl -s "$BASE_URL/api/users")
expect_count "users in /api/users" "$(count "$users" '"username"')" 10
expect_count "posts in /api/users" "$(count "$users" '"body"')" 100
expect_match "nested address.geo in /api/users" "$users" '"lat": "-37.3159"'
expect_match "indented JSON in /api/users" "$users" '^    "id": 1,$'

todos=$(count "$users" '"completed"')
if [ "$todos" = 200 ]; then
    printf 'PASS  todos in /api/users: 200\n'
else
    # Todos come from jsonplaceholder.typicode.com; without internet access users are returned without them.
    printf 'WARN  todos in /api/users: %s (is jsonplaceholder.typicode.com reachable?)\n' "$todos"
fi

posts=$(curl -s "$BASE_URL/api/posts")
expect_count "rows in /api/posts" "$(count "$posts" '<row>')" 100
expect_match "XML declaration in /api/posts" "$posts" '^<?xml version="1.0" encoding="UTF-8"?>'
expect_match "<root>/<row> with userId, id, title in /api/posts" "$posts" '^    <title>sunt aut facere'
expect_match "indented XML in /api/posts" "$posts" '^<root>$'

if [ "$failures" -ne 0 ]; then
    echo "$failures check(s) failed"
    exit 1
fi
echo "All checks passed"
