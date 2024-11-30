# Nginx-load-balancer

This module contains the files needed to start a [Nginx load-balancer](http://nginx.org/en/docs/http/load_balancing.html) component
configured with the [Ngx http geoip2 module](http://nginx.org/en/docs/http/ngx_http_geoip_module.html).

## To execute

#### Location

Ensure nginx is removed (it will be re-installed with the ngx_http_geoip2 module):

```sh
sudo apt remove nginx
```

Install the tool [libmaxminddb](https://github.com/maxmind/libmaxminddb):

```sh
LIBMAXMINDDB_VERSION=1.4.3 && \
wget https://github.com/maxmind/libmaxminddb/releases/download/${LIBMAXMINDDB_VERSION}/libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz && \
tar -xf libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz && \
cd libmaxminddb-${LIBMAXMINDDB_VERSION} && \
sudo ./configure && \
sudo make && \
sudo make check && \
sudo sudo make install && \
sudo ldconfig && \
cd.. && \
sudo rm -rf libmaxminddb-${LIBMAXMINDDB_VERSION} && \
```

Install and run [geoipupdate](https://github.com/maxmind/geoipupdate):

```sh
GEOIPUPDATE_VERSION=4.5.0 && \
sudo cp geoip/GeoIP.conf /usr/local/etc/ && \
wget https://github.com/maxmind/geoipupdate/releases/download/v${GEOIPUPDATE_VERSION}/geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz && \
tar -xf geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz && \
cp geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64/geoipupdate /usr/local/bin && \
rm -r geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64 && \
sudo mkdir /usr/local/share/GeoIP && \
sudo geoipupdate
```

Install nginx including the [ngx_http_geoip2](https://github.com/leev/ngx_http_geoip2_module) module:

```sh
NGINX_VERSION=1.19.4 && \
sudo apt install libmaxminddb0 libmaxminddb-dev mmdb-bin -y && \
git clone https://github.com/leev/ngx_http_geoip2_module.git && \
wget http://nginx.org/download/nginx-$NGINX_VERSION.tar.gz && \
tar -xf nginx-$NGINX_VERSION.tar.gz && \
cd nginx-$NGINX_VERSION && \
rm -f /etc/nginx/conf.d/* && \
mkdir /etc/nginx && \
sudo useradd -s /bin/false nginx && \
./configure --add-dynamic-module=../ngx_http_geoip2_module && \
make && \
sudo make install && \
sudo cp /usr/local/nginx/sbin/nginx /usr/sbin/nginx && \
cd.. && \
sudo rm -r nginx-$NGINX_VERSION.tar.gz nginx-$NGINX_VERSION ngx_http_geoip2_module && \
sudo nginx -t
```

####Docker

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer
docker run -p 1906:80 \
-e BASIC_AUTH_USERNAME=username \
-e BASIC_AUTH_PASSWORD=password \
nginx-load-balancer
```

##### With home servers
```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer
docker run -p 1906:80 \
-e BASIC_AUTH_USERNAME=username \
-e BASIC_AUTH_PASSWORD=password \
-e SERVERS="[{\"service\":\"app1\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097 ,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\" longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\ "202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
nginx-load-balancer
```

## Ngx http geoip2 module

The ngx_http_geoip2 module allows you to associate the client's IP address with a MaxMind database, thus enabling
extract customer geolocation values.

Include the module in the nginx configuration file:
```nginx
load_module "modules/ngx_http_geoip2_module.so";
```

Through the GeoLite2-City database, several values ​​associated with the IP address are available. In our case, we are interested in latitude and longitude:
```nginx
geoip2 /usr/local/share/GeoIP/GeoLite2-City.mmdb {
 $geoip2_location_latitude default=-1 location latitude;
 $geoip2_location_longitude default=-1 location longitude;
}
```

To see the values ​​associated with the IP address:
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip)
```

To view location values:
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location
```

To see a specific value, e.g. latitude:
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location latitude
```

## Nginx-load-balancer-api

The docker image includes the server [nginx-load-balancer-api](../nginx-load-balancer-api), to get/add/remove servers.
The load-balancer redirects requests it receives at the `_/api` location to nginx-load-balancer-api which
you are running on localhost.

Get the servers for all services:
```shell script
curl -i --user username:password http://localhost:1906/_/api/servers
```

Get the servers for service `app1`:
```shell script
curl -i --user username:password http://localhost:1906/_/api/app1/servers
```

Adding a server to the `app1` service:
```shell script
curl -i \
 --user username:password \
 --header "Content-Type: application/json" \
 --data '[{"server":"202.193.200.125:5
000","latitude":39.575097,"longitude":-8.909794,"region":"EUROPE"}]' \
 http://localhost:1906/_/api/app1/servers
```

Remove server `202.193.200.125:5000` from service `app1`:
```shell script
curl -i \
 --user username:password \
 -X DELETE \
 http://localhost:1906/_/api/app1/servers/202.193.200.125:5000
```

## Relevant modules

Currently not used, but possibly relevant:

- [ngx_http_lua_module](https://github.com/openresty/lua-nginx-module) - Embed the power of Lua into Nginx HTTP Servers
- [nginx-let-module](https://github.com/arut/nginx-let-module) - Adds support for arithmetic operations to NGINX configuration

## Features

- [geoipupdate](https://github.com/maxmind/geoipupdate) - GeoIP update program performs automatic updates of GeoIP2 and GeoIP Legacy binary databases

- [ngx_http_geoip2_module](https://github.com/leev/ngx_http_geoip2_module) - Creates variables with values ​​from maxmind geoip2 databases based on client IP (default) or from a specific variable (both IPv4 and IPv6 ports)

- [GeoLite2 Databases](https://dev.maxmind.com/geoip/geoip2/geolite2/) - GeoLite2 Databases are free IP geolocation databases


## License

Nginx-load-balancer is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
