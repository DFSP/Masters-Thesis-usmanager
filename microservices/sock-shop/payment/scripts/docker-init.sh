#!/bin/sh
#Script to launch processes

./app -port=$3 &
exec ./register -execapp=app -app=PAYMENT -autoregister=false -eureka=$1 -port=$2 -hostname=$4