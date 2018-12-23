#!/bin/sh
cd "$(dirname "$0")/server"
set -ue
if [[ $# -eq 0 ]]; then
  set -x
  exec sbt flywayMigrate test:flywayMigrate shell
else
  set -x
  exec sbt "$@"
fi
