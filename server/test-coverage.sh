#!/bin/sh
set -ue
cd $(dirname "$0")
exec sbt clean coverage test coverageReport
