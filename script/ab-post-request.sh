#!/bin/bash

set -uex
TMP_JSON=$(mktemp)
URL="http://localhost:18080/toy/reduceState"

echo '{ "delta": 2 }' > $TMP_JSON

exec ab -p "$TMP_JSON" -T application/json -c 100 -n 500 -r -k "$URL"
