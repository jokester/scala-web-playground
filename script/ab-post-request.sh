#!/bin/bash

set -uex


for URL in \
  '127.0.0.1:18080/toy/unsafe-mem/3' \
  '127.0.0.1:18080/toy/sync-mem/3'   \
  '127.0.0.1:18080/toy/db-basic/3'   \
  '127.0.0.1:18080/toy/db-nolock/3'
do
  ab -m POST -n 2000 -c 100 -r -k "$URL"
done
