# scala-server-playground

a playground for scala/akka web server

## Apps

### chat-room

- (non-persistent) websocket chat room
- Web UI made with React + MUI

### etc

and some example code to try `akka / scalikejdbc / pgsql / redis` combination

## Dev

### Start DBs

```sh
# run pgsql / redis containers

cd dev-db && docker-compose up -d
```

### PgSQL (shell)

```sh
# create db:

createdb -h 127.0.0.1 -p 65432 --username=pguser scala_playground_dev
createdb -h 127.0.0.1 -p 65432 --username=pguser scala_playground_test

# inspect db:

psql -h 127.0.0.1 -p 65432 --username=pguser scala_playground_dev
```

### PgSQL (sbt)

```
# apply latest schema:

sbt> flywayMigrate
sbt> test:flywayMigrate

# clean schema:

sbt> flywayClean
sbt> test:flywayClean
```

### Redis

```
redis-cli -p 59379
```

## LICENSE

WTFPL

