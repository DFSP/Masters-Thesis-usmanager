#!/bin/sh
#Script to launch processes

./catalogue -port=$3 -DSN=$5 &
exec ./registration-client -process=app -service=CATALOGUE -register=false -server=$1 -port=$2 -hostname=$4
