#!/bin/sh

set -e

if [ -z $SERVER1 ]; then
  echo >&2 "SERVER1 must be set"
  exit 1
fi

if [ -z $BASIC_AUTH_USERNAME ]; then
  echo >&2 "BASIC_AUTH_USERNAME must be set"
  exit 1
fi

if [ -z $BASIC_AUTH_PASSWORD ]; then
  echo >&2 "BASIC_AUTH_PASSWORD must be set"
  exit 1
fi

htpasswd -bBc /etc/nginx/.htpasswd $BASIC_AUTH_USERNAME $BASIC_AUTH_PASSWORD
sed \
  -e "s/##SERVER1##/$SERVER1/g" \
  -e "s/##SERVER_NAME##/$SERVER_NAME/g" \
  nginx.conf.tmpl > /etc/nginx/nginx.conf

./nginx-load-balancer-api & exec nginx -g "daemon off;"