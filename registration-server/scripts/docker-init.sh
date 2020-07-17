#!/bin/sh
#Script to launch processes

exec java -Djava.security.egd=file:/dev/urandom -jar ./app.jar --port=$2 --host=$3 --ip=$3 --id=eureka-server_$3_$1 --zone=$4