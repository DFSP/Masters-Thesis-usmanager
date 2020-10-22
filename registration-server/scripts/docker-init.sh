#!/bin/sh
#Script to launch processes

if [ -z "${internalPort}" ] || [ -z "${externalPort}" ] || [ -z "${hostname}" ]; then
  echo "Usage: sh $(basename "$0") internalPort externalPort hostname"
  exit
fi

id="$(exec echo -n "eureka-server_${hostname}_${externalPort}" | sha1sum | awk '{print $1}')"

exec java -Djava.security.egd=file:/dev/urandom -jar ./registration-server.jar \
  --port="${internalPort}" \
  --host="${hostname}" \
  --ip="${hostname}" \
  --id="${id}" \
  --zone=http://"${hostname}":"${externalPort}"/eureka/
