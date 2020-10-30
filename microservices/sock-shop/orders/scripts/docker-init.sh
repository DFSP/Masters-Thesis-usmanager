#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./orders.jar --port=$3 --db=$5 &
exec ./registration-client -process=java -service=ORDERS -register=false -server=$1 -port=$2 -hostname=$4
