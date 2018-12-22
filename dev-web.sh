#!/bin/bash

set -ue
cd "$(dirname "$0")/web"
source env-dev

set | grep REACT_APP

exec yarn start
