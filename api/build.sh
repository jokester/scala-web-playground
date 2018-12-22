#!/bin/bash

set -ue -x

cd "$(dirname "$0")"

build-api () {
  local name="$1"
  mkdir -pv "$name-ts.gen"

  protoc \
    -I/usr/local/include \
    -I. \
    -I$GOPATH/src \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway/third_party/googleapis \
    -I$GOPATH/src/github.com/grpc-ecosystem/grpc-gateway \
    --swagger_out=logtostderr=true,grpc_api_configuration="./src/$name.yml":. \
    src/"$name.proto"

  swagger-codegen generate \
    -i "./src/$name.swagger.json" \
    -l typescript-fetch \
    -c swagger-codegen.json \
    -o "./$name-ts.gen"
}

build-api "chat2"
