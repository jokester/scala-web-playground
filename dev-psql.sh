#!/bin/bash

set -ue

DB_SERVER='127.0.0.1'
DB_PORT='65432'
DB_USER='pguser'
DB_PWD='secret'

if [[ $# -eq 1 ]]; then
  echo "using DB_NAME=$1"
  exec psql "postgresql://$DB_USER:$DB_PWD@$DB_SERVER:$DB_PORT/$1"
else
  exec psql "postgresql://$DB_USER:$DB_PWD@$DB_SERVER:$DB_PORT/scala_playground_dev"
fi
