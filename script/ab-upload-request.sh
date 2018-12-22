#!/bin/bash

set -uex
URL="http://localhost:18080/blob"

ab -c 100 -n 500 -v 1 -p "$1" -T "multipart/form-data; boundary=miuxupsktcqtriloonfbdudrgtawascl" "$URL"
