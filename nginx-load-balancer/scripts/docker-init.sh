#!/bin/sh

set -e

if [ -z "$BASIC_AUTH_USERNAME" ]; then
  echo >&2 "BASIC_AUTH_USERNAME must be set"
  exit 1
fi

if [ -z "$BASIC_AUTH_PASSWORD" ]; then
  echo >&2 "BASIC_AUTH_PASSWORD must be set"
  exit 1
fi

htpasswd -bBc /etc/nginx/.htpasswd "$BASIC_AUTH_USERNAME" "$BASIC_AUTH_PASSWORD"

if [ -z ${SERVER_NAME+x} ]; then
  echo "Using default server_name: load-balancer.com"
else
  sed -i "s/load-balancer.com/$SERVER_NAME/g" nginx.conf
  echo "Using server_name: $SERVER_NAME"
fi

cp nginx.conf /usr/local/nginx/conf/nginx.conf

./nginx-load-balancer-api &
exec nginx -g "daemon off;"
