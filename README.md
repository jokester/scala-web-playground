# scala-web-playground

a playground for scala/akka web server

## DBs

run DB servers:

```
cd dev-db
docker-compose up -d
```

init db:

```
createdb -h 127.0.0.1 -p 65432 --username=pguser scala_playground_dev
createdb -h 127.0.0.1 -p 65432 --username=pguser scala_playground_test
```

apply migrations:

```
sbt> flywayMigrate
sbt> test:flywayMigrate
```

inspect db:

```
psql -h 127.0.0.1 -p 65432 --username=pguser scala_playground_dev
```

clean db:

```
sbt> flywayClean
sbt> test:flywayClean
```


### PGSQL


### Redis


## chat-room

- (non-persistent) websocket chat room
- web UI made with React + MUI

## hanhuazu

WIP

## LICENSE

WTFPL

