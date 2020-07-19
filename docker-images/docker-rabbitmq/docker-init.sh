#!/bin/sh
#Script to launch processes

./usr/local/bin/docker-entrypoint.sh rabbitmq-server &
exec ./registration-client -execapp=rabbitmq -app=RABBITMQ -eureka=$1 -port=$2 -hostname=$4