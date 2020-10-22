#!/bin/sh
#Script to launch processes

./app -port=$3 &
exec ./registration-client -process=app -service=PAYMENT -register=false -eureka=$1 -port=$2 -hostname=$4
