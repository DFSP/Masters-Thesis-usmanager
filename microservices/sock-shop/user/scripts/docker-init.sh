#!/bin/sh
#Script to launch processes

./user -port=$3 -mongo-host=$5 &
exec ./registration-client -process=app -service=USER -register=false -server=$1 -port=$2 -hostname=$4
