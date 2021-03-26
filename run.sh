#!/usr/bin/env bash

set -e

mvn clean package -U -Dmaven.test.skip=true
docker-compose down
echo "Start all containers"
docker-compose  up -d --build
echo "New Release is Ready"
docker logs --follow eventstore






