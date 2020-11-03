# Nginx-load-balancer

Este módulo contém os ficheiros necessários para iniciar um componente [Nginx load-balancer](http://nginx.org/en/docs/http/load_balancing.html) 
configurado com o [modulo Ngx http geoip2](http://nginx.org/en/docs/http/ngx_http_geoip_module.html).

## Executar

#### Local

Garantir que o nginx é removido (irá ser re-instalado com o módulo ngx_http_geoip2):

```sh
sudo apt remove nginx
```

Instalar a ferramenta [libmaxminddb](https://github.com/maxmind/libmaxminddb):

```sh
LIBMAXMINDDB_VERSION=1.4.3
wget https://github.com/maxmind/libmaxminddb/releases/download/${LIBMAXMINDDB_VERSION}/libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz
tar -xf libmaxminddb-${LIBMAXMINDDB_VERSION}.tar.gz
cd libmaxminddb-${LIBMAXMINDDB_VERSION}
./configure
make
make check
sudo make install
sudo ldconfig /
```

Instalar e executar o [geoipupdate](https://github.com/maxmind/geoipupdate):

```sh
GEOIPUPDATE_VERSION=4.5.0
sudo cp geoip/GeoIP.conf /usr/local/etc/
wget https://github.com/maxmind/geoipupdate/releases/download/v${GEOIPUPDATE_VERSION}/geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz
tar -xf geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64.tar.gz
cp geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64/geoipupdate /usr/local/bin
rm -r geoipupdate_${GEOIPUPDATE_VERSION}_linux_amd64
sudo mkdir /usr/local/share/GeoIP
sudo geoipupdate
```

Instalar o nginx incluindo o módulo [ngx_http_geoip2](https://github.com/leev/ngx_http_geoip2_module):

```sh
NGINX_VERSION=1.19.4
sudo apt install libmaxminddb0 libmaxminddb-dev mmdb-bin -y
git clone https://github.com/leev/ngx_http_geoip2_module.git
wget http://nginx.org/download/nginx-$NGINX_VERSION.tar.gz
tar -xf nginx-$NGINX_VERSION.tar.gz
cd nginx-$NGINX_VERSION
rm -f /etc/nginx/conf.d/*
mkdir /etc/nginx
sudo useradd -s /bin/false nginx
./configure --add-dynamic-module=../ngx_http_geoip2_module
make
sudo make install
cp /usr/local/nginx/sbin/nginx /usr/sbin/nginx
cd ..
rm -r nginx-$NGINX_VERSION.tar.gz nginx-$NGINX_VERSION ngx_http_geoip2_module
nginx -t
```

#### Docker
 
```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer  
docker run -p 1907:80 \
-e BASIC_AUTH_USERNAME=username \
-e BASIC_AUTH_PASSWORD=password \
nginx-load-balancer 
```

##### Com servidores iniciais
```shell script
docker run -p 1907:80 \
-e BASIC_AUTH_USERNAME=username \
-e BASIC_AUTH_PASSWORD=password \
-e SERVERS="[{\"service\":\"app\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\"202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
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
geoip2 /usr/local/share/GeoIP/GeoLite2-City.mmdb {
    $geoip2_location_latitude default=-1 location latitude;
    $geoip2_location_longitude default=-1 location longitude;
}
```

Para ver os valores associados ao endereço ip:
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip)
```

Para ver os valores de localização: 
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location
```

Para ver um valor especifico, e.g. latitude:
```sh
sudo mmdblookup --file /usr/share/GeoIP/GeoLite2-City.mmdb --ip $(curl https://ipinfo.io/ip) location latitude
```

## Nginx-load-balancer-api

A imagem docker inclui o servidor [nginx-load-balancer-api](../nginx-load-balancer-api), para obter/adicionar/remover servidores.
O load-balancer redireciona os pedidos que recebe na localização `_/api` para o nginx-load-balancer-api que
está a executar no localhost.

Obter os servidores de todos os serviços:
```shell script
curl -i --user username:password http://localhost:1907/_/api/servers
```

Obter os servidores do serviço `app`:
```shell script
curl -i --user username:password http://localhost:1907/_/api/app/servers
```

Adicionar um servidor ao serviço `app`:
```shell script
curl -i \
     --user username:password \
     --header "Content-Type: application/json" \
     --data '[{"server":"202.193.200.125:5000","latitude":39.575097,"longitude":-8.909794,"region":"EUROPE"}]' \
     http://localhost:1907/_/api/app/servers
```

Remover o servidor `202.193.200.125:5000` ao serviço `app`:
```shell script
curl -i \
     --user username:password \
     -X DELETE \
     http://localhost:1907/_/api/apps/servers/202.193.200.125:5000
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
