#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./orders.jar --port=$3 --db=$5 &
exec ./registration-client -execapp=java -app=ORDERS -autoregister=false -eureka=$1 -port=$2 -hostname=$4