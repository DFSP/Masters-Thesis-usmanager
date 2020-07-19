#!/bin/sh
#Script to launch processes

./app -port=$3 -DSN=$5 &
exec ./registration-client -execapp=app -app=CATALOGUE -autoregister=false -eureka=$1 -port=$2 -hostname=$4