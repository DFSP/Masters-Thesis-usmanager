#!/bin/sh
#Script to launch processes

externalPort=$1
internalPort=$2
hostname=$3
exec java -Djava.security.egd=file:/dev/urandom -jar ./registration-server.jar \
  --port="$internalPort" \
  --host="$hostname" \
  --ip="$hostname" \
  --id=eureka-server_"$hostname"_"$externalPort" \
  --zone=http://"$hostname":"$externalPort"/eureka/
