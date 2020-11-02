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
**Get** /{service}/servers | Lista todos os servidores do serviço `{service}` registados neste load balancer
**POST** /{service}/servers | Adiciona servidores novos ao serviço `{service}`. Pedido: `[{server, latitude, longitude, region}]`
**DELETE** /{service}/servers/{server} | Remove o servidor `{server}` do serviço `{service}`

## Exemplos

Obter os servidores do serviço `app`:
```shell script
curl -i http://localhost:1907/_/nginx-load-balancer-api/app/servers
```

Adicionar um servidor ao serviço `app`:
```shell script
curl -i \
     --header "Content-Type: application/json" \
     --data '[{"server":"202.193.200.125:5000","latitude":39.575097,"longitude":-8.909794,"region":"EUROPE"}' \
     http://localhost:1907/_/nginx-load-balancer-api/app/servers
```

Remover o servidor `202.193.200.125:5000` ao serviço `app`:
```shell script
curl -i -X DELETE http://localhost:1907/_/nginx-load-balancer-api/app/servers/202.193.200.125:5000
```

## Licença

Nginx-load-balancer-api está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.