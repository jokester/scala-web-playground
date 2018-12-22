#!/bin/bash

set -uex

URL="http://127.0.0.1:18080/toy/readState"

exec ab -n 200 -c 10 -r -k "$URL"
