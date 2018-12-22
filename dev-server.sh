#!/bin/sh
cd "$(dirname "$0")/server"
# exec sbt flywayMigrate test:flywayMigrate shell
exec sbt shell
