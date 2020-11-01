FROM nginx-load-balancer-api as nginx-load-balancer-api
FROM nginx:alpine

# for htpasswd command
RUN apk add --no-cache --update apache2-utils
RUN rm -f /etc/nginx/conf.d/*

# include maxmind database to retrieve information from ip addresses
RUN mkdir -p /etc/nginx/geoip && \
wget https://dl.miyuru.lk/geoip/maxmind/city/maxmind.dat.gz -O /etc/nginx/geoip/city.dat.gz && \
gunzip /etc/nginx/geoip/city.dat.gz

# ENV ALG round-robin

COPY --from=nginx-load-balancer-api /nginx-load-balancer-api ./nginx-load-balancer-api
COPY /scripts/docker-init.sh /
COPY /nginx.conf /

# use SIGQUIT for graceful shutdown
# c.f. http://nginx.org/en/docs/control.html
STOPSIGNAL SIGQUIT

RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["/docker-init.sh"]