# Nginx-load-balancer

Este módulo contém os ficheiros necessários para iniciar um componente [Nginx load-balancer](http://nginx.org/en/docs/http/load_balancing.html).
configurado com o módulo geoip

## Módulo Nginx geoip

O módulo [ngx_http_geoip_module](http://nginx.org/en/docs/http/ngx_http_geoip_module.html)
cria variáveis com valores associados ao ip do client, usando as bases de dados MaxMind.

Através da diretiva `geoip_city` ficam disponíveis vários [valores](http://nginx.org/en/docs/http/ngx_http_geoip_module.html#geoip_city), dos quais são usados os seguintes:
- $geoip_latitude - dados sobre a latitude aproximada do cliente
- $geoip_longitude - dados sobre a longitude aproximada do cliente

## Executar

#### Docker
 
```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer  
docker run --rm -p 1906:80 nginx-load-balancer
```

## Licença

Nginx-load-balancer está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.