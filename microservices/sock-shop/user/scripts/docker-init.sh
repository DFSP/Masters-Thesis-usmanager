#!/bin/sh
#Script to launch processes

./user -port=$3 -mongo-host=$5 &
exec ./registration-client -execapp=app -app=USER -autoregister=false -eureka=$1 -port=$2 -hostname=$4
