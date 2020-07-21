# Request location monitor

Guarda o número de pedidos de um serviço, com os detalhes da localização.

#### Executar

```shell script
cd request-location-monitor/cmd
go build -o request-location-monitor
./request-location-monitor
```

O resultado é o ficheiro binário `request-location-monitor`, gerado na diretoria atual.

#### Docker

```shell script
docker build -f docker/Dockerfile . -t request-location-monitor
docker run --rm -p 1919:1919 request-location-monitor
```

#### API Endpoints

Os URIs são relativos a *http://localhost:1919/api*

HTTP request | Description
------------ | -------------
**Get** /monitoringinfo | List all monitoring info
**Get** /monitoringinfo/{serviceName} | List monitoring info by service
**Get** /monitoringinfo/{serviceName}/top | List monitoring info by service, with top location requests

#### License

Request-location-monitor está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.