#!/bin/sh
#Script to launch processes

./usr/local/bin/docker-entrypoint.sh rabbitmq-server &
exec ./registration-client -process=rabbitmq -service=RABBITMQ -server=$1 -port=$2 -hostname=$4
