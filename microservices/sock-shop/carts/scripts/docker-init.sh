#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./carts.jar --port=$3 --db=$5 &
exec ./registration-client -execapp=java -app=CARTS -autoregister=false -eureka=$1 -port=$2 -hostname=$4
