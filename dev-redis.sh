#!/bin/bash

set -ue

DB_SERVER='127.0.0.1'
DB_PORT='59379'
DB_PWD='secret'

exec redis-cli -h "$DB_SERVER" -p "$DB_PORT"
