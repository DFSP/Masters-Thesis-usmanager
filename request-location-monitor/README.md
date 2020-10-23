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
**Get** /monitoring?aggregation&interval={ms} | Lista toda a monitorização registada agregada por serviço, nos últimos {ms} milisegundos
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
curl http://localhost:1919/api/monitoring?aggregation&interval=120000
```

Regista uma nova monitorização de um serviço:
```shell script
curl --header "Content-Type: application/json" \
     --data '{"service":"app","latitude":39.575097,"longitude":-8.909794,"count":1}' \
     http://localhost:1919/api/monitoring
```

## Ferramentas

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The Collaboration Platform for API Development

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore/detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP nicely when you visit it 'directly' in a browser tab

## License

Request-location-monitor está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.