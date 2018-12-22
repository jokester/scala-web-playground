#!/bin/bash

set -uex

for URL in \
  '127.0.0.1:18080/toy/unsafe-mem' \
  '127.0.0.1:18080/toy/sync-mem' \
  '127.0.0.1:18080/toy/db-basic'  \
  '127.0.0.1:18080/toy/db-nolock'
do
  ab -n 2000 -c 100 -r -k "$URL"
done
