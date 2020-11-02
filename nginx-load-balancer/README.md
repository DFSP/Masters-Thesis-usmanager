# Nginx-load-balancer

Este módulo contém os ficheiros necessários para iniciar um componente [Nginx load-balancer](http://nginx.org/en/docs/http/load_balancing.html) 
configurado com o [modulo Ngx http geoip2](http://nginx.org/en/docs/http/ngx_http_geoip_module.html).

## Executar

#### Local

Garantir que o nginx é removido (irá ser re-instalado com o módulo ngx_http_geoip2):
```console
sudo apt remove nginx
```

Instalar e executar o [geoipupdate](https://github.com/maxmind/geoipupdate):

```console
sudo cp geoip/GeoIP.conf /etc/GeoIP.conf
sudo apt install geoipupdate -y
sudo geoipupdate
sudo ls /usr/share/GeoIP/
```

Instalar o nginx incluindo o módulo [ngx_http_geoip2](https://github.com/leev/ngx_http_geoip2_module):
```shell script
NGINX_VERSION=1.19.4
sudo apt install libmaxminddb0 libmaxminddb-dev mmdb-bin -y
git clone https://github.com/leev/ngx_http_geoip2_module.git
wget http://nginx.org/download/nginx-$NGINX_VERSION.tar.gz
tar -xf nginx-$NGINX_VERSION.tar.gz
cd nginx-$NGINX_VERSION
sudo rm -f /etc/nginx/conf.d/*
sudo mkdir /etc/nginx
./configure --add-dynamic-module=../ngx_http_geoip2_module
make
make install
cp /usr/local/nginx/sbin/nginx /usr/sbin/nginx
cd ..
rm -r nginx-$NGINX_VERSION.tar.gz nginx-$NGINX_VERSION ngx_http_geoip2_module
nginx -t
```

#### Docker
 
```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer  
docker run -p 1908:80 -p 1907:1907 \
-e BASIC_AUTH_USERNAME=username \
-e BASIC_AUTH_PASSWORD=password \
-e SERVER=test:3000 \
nginx-load-balancer 
```

## Módulo Ngx http geoip2

O módulo ngx_http_geoip2 permite associar o endereço ip do cliente a uma base de dados MaxMind, possibilitando assim
extrair valores de geolocalização do cliente.

Incluir o módulo no ficheiro de configuração nginx:
```nginx
load_module "modules/ngx_http_geoip2_module.so";
```

Através da base de dados GeoLite2-City, ficam disponíveis vários valores associados ao endereço ip. No nosso caso, interessam-nos a latitude e a longitude: 
```nginx
geoip2 /usr/share/GeoIP/GeoLite2-City.mmdb {
  $geoip2_location_latitude default=-1 location latitude;
  $geoip2_location_longitude default=-1 location longitude;
}
```

Para ver os valores associados ao endereço ip:
```bash
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip)
```

Para ver os valores de localização: 
```bash
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location
```

Para ver um valor especifico, e.g. latitude:
```bash
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location latitude
```

## Módulos relevantes 

Atualmente não utilizados, mas possivelmente relevantes:

- [ngx_http_lua_module](https://github.com/openresty/lua-nginx-module) - Embed the power of Lua into Nginx HTTP Servers
- [nginx-let-module](https://github.com/arut/nginx-let-module) - Adds support for arithmetic operations to NGINX config

## Recursos

- [geoipupdate](https://github.com/maxmind/geoipupdate) - The GeoIP Update program performs automatic updates of GeoIP2 and GeoIP Legacy binary databases

- [ngx_http_geoip2_module](https://github.com/leev/ngx_http_geoip2_module) - Creates variables with values from the maxmind geoip2 databases based on the client IP (default) or from a specific variable (supports both IPv4 and IPv6)

- [GeoLite2 Databases](https://dev.maxmind.com/geoip/geoip2/geolite2/) - GeoLite2 databases are free IP geolocation databases 


## Licença

Nginx-load-balancer está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.