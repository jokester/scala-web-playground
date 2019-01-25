#!/bin/sh
cd "$(dirname "$0")/server"
set -ue
if [[ $# -eq 0 ]]; then
  set -x
  exec sbt shell
else
  set -x
  exec sbt "$@"
fi
