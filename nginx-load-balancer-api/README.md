# Nginx load balancer API

API para adicionar ou remover servidores ao [Nginx load balancer](../nginx-load-balancer) e atualizar os ficheiros de configuração.

## Argumentos

Usage of ./nginx-load-balancer-api:

- delay (int)

    Update delay (in seconds) of the nginx configuration after adding a new server (default 15)
        
- port (int)

    Port to bind HTTP listener (default 1906)

O nginx-load-balancer-api pode ser inicializado com servidores, através duma variável ambiente em formato json, e.g.:

```json
[
  {
    "service": "app",
    "servers": [
      {
        "server": "202.193.200.125:5000",
        "latitude": 39.575097,
        "longitude": -8.909794,
        "region": "EUROPE"
      },
      {
        "server": "202.193.20.125:5000",
        "latitude": 39.575097,
        "longitude": -8.909794,
        "region": "EUROPE"
      }
    ]
  },
  {
    "service": "app2",
    "servers": [
      {
        "server": "202.193.203.125:5000",
        "latitude": 39.575097,
        "longitude": -8.909794,
        "region": "EUROPE"
      }
    ]
  }
]
```

## Executar

#### Local

```shell script
sudo nginx 
go build -o nginx-load-balancer-api
sudo ./nginx-load-balancer-api
```
Com servidores iniciais
```shell script
sudo env SERVERS="[{\"service\":\"app\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\"202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
./nginx-load-balancer-api
```

#### Docker

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer-api
docker run -p 1906:1906 nginx-load-balancer-api
```

Com servidores iniciais

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer-api
docker run -p 1906:1906 \
-e SERVERS="[{\"service\":\"app\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\"202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
nginx-load-balancer-api
```

## API Endpoints

Os URIs são relativos a *http://localhost:1906/api*

HTTP request | Description
------------ | -------------
**Get** /servers | Obter os servidores de todos os serviços registados neste load balancer
**Get** /{service}/servers | Lista todos os servidores do serviço `{service}` registados neste load balancer
**POST** /{service}/servers | Adiciona servidores novos ao serviço `{service}`. Pedido: `[{server, latitude, longitude, region}]`
**DELETE** /{service}/servers/{server} | Remove o servidor `{server}` do serviço `{service}`

## Exemplos

Obter os servidores de todos os serviços:
```shell script
curl -i --user username:password http://localhost:1906/api/servers
```

Obter os servidores do serviço `app`:
```shell script
curl -i --user username:password http://localhost:1906/api/app/servers
```

Adicionar um servidor ao serviço `app`:
```shell script
curl -i \
     --user username:password \
     --header "Content-Type: application/json" \
     --data '[{"server":"202.193.200.125:5000","latitude":39.575097,"longitude":-8.909794,"region":"EUROPE"}]' \
     http://localhost:1906/api/app/servers
```

Remover o servidor `202.193.200.125:5000` ao serviço `app`:
```shell script
curl -i \
     --user username:password \
     -X DELETE \
     http://localhost:1906/api/apps/servers/202.193.200.125:5000
```

## Licença

Nginx-load-balancer-api está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.