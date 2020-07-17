#!/bin/sh
#Script to launch processes

npm start eureka=$1 &
exec ./register -execapp=node -app=FRONT-END -eureka=$1 -port=$2 -hostname=$4