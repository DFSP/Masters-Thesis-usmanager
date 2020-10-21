# Request location monitor

Guarda o número de pedidos de um serviço, com os detalhes da localização.

## Argumentos

Usage of ./request-location-monitor:

  - port (int)
  
    Port to bind HTTP listener (default 1919)

  - interval (int)
  
    Default interval (in ms) to include instances on data aggregation (default 60000)

## Executar

#### Local
```shell script
go build -o request-location-monitor
./request-location-monitor
```

#### Docker

```shell script
docker build -f docker/Dockerfile . -t request-location-monitor
docker run --rm -p 1919:1919 request-location-monitor
```

## API Endpoints

Os URIs são relativos a *http://localhost:1919/api*

HTTP request | Description
------------ | -------------
**Get** /monitoring | Lista toda a monitorização registada
**Get** /monitoring?aggregation | Lista toda a monitorização registada agregada por serviço, nos últimos 60 segundos
**Get** /monitoring?aggregation&interval={seconds} | Lista toda a monitorização registada agregada por serviço, nos últimos {seconds} segundos
**Post** /monitoring | Adiciona uma nova monitorização: `{service, latitude, longitude, count}`

## Exemplo

Ver todas as monitorizações:
```shell script
curl http://localhost:1919/api/monitoring
```

Ver todas as monitorizações, no últimos 60 segundos, agregadas por serviço:
```shell script
curl http://localhost:1919/api/monitoring?aggregation
```

Ver todas as monitorizações, no últimos 120 segundos, agregadas por serviço:
```shell script
curl http://localhost:1919/api/monitoring?aggregation&interval=120
```

Regista uma nova monitorização de um serviço:
```shell script
curl --header "Content-Type: application/json" \
     --data '{"service":"app","latitude":"39.575097","longitude":"-8.909794","count":"1"}' \
     http://localhost:1919/api/monitoring
```

## License

Request-location-monitor está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.