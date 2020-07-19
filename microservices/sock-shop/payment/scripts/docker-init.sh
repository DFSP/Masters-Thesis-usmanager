#!/bin/sh
#Script to launch processes

./app -port=$3 &
exec ./registration-client -execapp=app -app=PAYMENT -autoregister=false -eureka=$1 -port=$2 -hostname=$4
