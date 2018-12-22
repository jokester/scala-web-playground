#!/bin/bash

set -uex

URL="127.0.0.1:18080/toy/unsafe-mem"

exec ab -n 200 -c 10 -r -k "$URL"
