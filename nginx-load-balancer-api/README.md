# Nginx load balancer API

API para adicionar ou remover servidores ao [Nginx load balancer](../nginx-load-balancer) e atualizar os ficheiros de configuração.

## Argumentos

Usage of ./nginx-load-balancer-api:

- delay (int)

    Update delay (in seconds) of the nginx configuration after adding a new server (default 15)
        
- port (string)

    Port to bind HTTP listener (default "1907")

## Executar

##### Local

```shell script
go build -o nginx-load-balancer-api
sudo ./nginx-load-balancer-api
```

##### Docker

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer-api
docker run -p 1907:1907 nginx-load-balancer-api
```

## API Endpoints

Os URIs são relativos a *http://localhost:1906/_/nginx-load-balancer-api/api*

HTTP request | Description
------------ | -------------
**Get** /servers | Lista todos os servidores registados neste load balancer
**POST** /servers | Adiciona servidores novos. Pedido: `[{hostname, latitude, longitude, region}]`
**DELETE** /servers/{hostname} | Remove o servidor `{hostname}`

## Licença

Nginx-load-balancer-api está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.