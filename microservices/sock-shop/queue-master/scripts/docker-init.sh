#!/bin/sh
#Script to launch processes

java -Djava.security.egd=file:/dev/urandom -jar ./queue-master.jar --port=$3 --rabbithost=$5 &
exec ./registration-client -process=java -service=QUEUE-MASTER -register=false -server=$1 -port=$2 -hostname=$4
