#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./app.jar --port=$3 --rabbithost=$5 &
exec ./registration-client -execapp=java -app=SHIPPING -autoregister=false -eureka=$1 -port=$2 -hostname=$4