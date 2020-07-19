FROM usmanager/nginx-load-balancer-api
FROM nginx:alpine

# for htpasswd command
RUN apk add --no-cache --update \
      apache2-utils
RUN rm -f /etc/nginx/conf.d/*

RUN mkdir -p /etc/nginx/geoip
RUN wget https://dl.miyuru.lk/geoip/maxmind/country/maxmind.dat.gz -O /etc/nginx/geoip/country.dat.gz
RUN wget https://dl.miyuru.lk/geoip/maxmind/city/maxmind.dat.gz -O /etc/nginx/geoip/city.dat.gz
RUN gunzip /etc/nginx/geoip/country.dat.gz
RUN gunzip /etc/nginx/geoip/city.dat.gz

ENV SERVER_NAME load-balancer.com
# ENV ALG round-robin

COPY --from=0 /nginx-load-balancer-api ./nginx-load-balancer-api
COPY /files/run.sh /
COPY /files/nginx.conf.tmpl /

# use SIGQUIT for graceful shutdown
# c.f. http://nginx.org/en/docs/control.html
STOPSIGNAL SIGQUIT

RUN ["chmod", "+x", "run.sh"]
ENTRYPOINT ["/run.sh"]
