#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./app.jar --port=$3 --rabbithost=$5 &
exec ./registration-client -process=java -service=SHIPPING -register=false -server=$1 -port=$2 -hostname=$4
