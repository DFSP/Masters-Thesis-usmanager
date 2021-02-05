FROM usmanager/nginx-load-balancer-api AS nginx-load-balancer-api
FROM alpine AS BUILD

# install libmaxminddb tool
ARG LIBMAXMINDDB_VERSION=1.4.3
RUN apk add --no-cache --update tar make g++ perl && \
wget https://github.com/maxmind/libmaxminddb/releases/download/${LIBMAXMINDDB_VERSION}/libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz && \
tar -xf libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz && \
cd libmaxminddb-${LIBMAXMINDDB_VERSION} && \
mkdir /usr/bin/file && \
./configure && \
make && \
make check && \
make install && \
ldconfig / && \
apk del tar make g++ perl

# install and run geoipupdate
ARG GEOIPUPDATE_VERSION=4.5.0
COPY geoip/GeoIP.conf /usr/local/etc/
RUN wget https://github.com/maxmind/geoipupdate/releases/download/v${GEOIPUPDATE_VERSION}/geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz && \
tar -xf geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz && \
cp geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64/geoipupdate /usr/local/bin && \
rm -r geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64 && \
mkdir /usr/local/share/GeoIP && \
geoipupdate

# install nginx with geoip2 dynamic module to retrieve information from ip addresses
ARG NGINX_VERSION=1.19.4
RUN apk add --no-cache --update tar make g++ perl pcre-dev zlib-dev && \
wget https://github.com/leev/ngx_http_geoip2_module/archive/3.3.tar.gz -O ngx_http_geoip2_module.tar.gz && \
tar -xf ngx_http_geoip2_module.tar.gz && \
wget http://nginx.org/download/nginx-$NGINX_VERSION.tar.gz && \
tar -xf nginx-$NGINX_VERSION.tar.gz && \
cd nginx-$NGINX_VERSION && \
rm -f /etc/nginx/conf.d/* && \
mkdir /etc/nginx && \
addgroup -g 101 -S nginx && \
adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -G nginx -g nginx nginx && \
./configure --add-dynamic-module=../ngx_http_geoip2_module-3.3 && \
make && \
make install && \
cp /usr/local/nginx/sbin/nginx /usr/sbin/nginx && \
cd .. && \
rm -r nginx-$NGINX_VERSION.tar.gz nginx-$NGINX_VERSION ngx_http_geoip2_module-3.3 && \
nginx -t && \
apk del tar make g++ perl zlib-dev

# packages needed: htpasswd from apache2-utils and pcre from pcre-dev
RUN apk add --no-cache --update apache2-utils

COPY --from=nginx-load-balancer-api /nginx-load-balancer-api /nginx-load-balancer-api
COPY /scripts/docker-init.sh /
COPY /nginx.conf /

# use SIGQUIT for graceful shutdown
# c.f. http://nginx.org/en/docs/control.html
STOPSIGNAL SIGQUIT

RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["/docker-init.sh"]
