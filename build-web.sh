#!/bin/bash

set -ue
cd "$(dirname "$0")/web"
source env-prod

exec yarn build
