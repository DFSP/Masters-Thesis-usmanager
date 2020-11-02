# Registration-client

Regista o serviço no [Servidor Eureka](../registration-server), e obtém o endpoints dos outros serviços, também registados no servidor.
Usa as distâncias entre os endpoints no algoritmo de escolha.

## Argumentos

Usage of ./registration-client:

  - service (string) *
  
    Service name

  - latitude (float) *
  
    Service Latitude
    
  - longitude (float) *
  
    Service Longitude

   - hostname (string)
     
    Service Hostname (default "localhost")
   
  - port (int)
  
    Service Port (default 1906)

  - process (string)
  
    Process name to monitor
    
  - cache-time (int)
  
    Time (in ms) to cache instances endpoints before contacting Eureka (default 10000)
  
  - eureka (string)
  
    Eureka server (default "127.0.0.1:8761")
        
  - interval (int)
  
    Interval time (in ms) to send location data (default 5000)
    
  - register (bool)
  
    True: registration-client will register service on Eureka; False: service must manually trigger the register (default true)

## Executar

### Local

```shell script
go build -o registration-client
./registration-client -service app -latitude 38.660758 -longitude -9.203568
```

O resultado é o ficheiro binário `registration-client`, gerado na diretoria atual.

### Docker

```shell script
docker build -f docker/Dockerfile . -t registration-client
docker run -p 1906:1906 -e service=app -e latitude=38.660758 -e longitude=-9.203568 -e eureka={publicIp}:8761 registration-client
```

## API Endpoints

Os URIs são relativos a *http://localhost:1906/api*

HTTP request | Description
------------ | -------------
**Post** /register | Regista o endpoint no servidor eureka
**Get** /services/{service}/endpoint | Obtém o melhor endpoint para o serviço {service}
**Get** /services/{service}/endpoints | Obtém todos os endpoints registados em nome do serviço {service}
**Post** /metrics | Adiciona uma nova monitorização deste endpoint. Request body: `{service, latitude, longitude, count}`

## Exemplo

Registar manualmente o endpoint:
```shell script
curl -i -X POST http://localhost:2525/api/register
```

Obtém o melhor endpoint para o serviço app
```shell script
curl -i http://localhost:2525/services/app/endpoint
```

Obtém todos os endpoints do serviço app
```shell script
curl -i http://localhost:2525/services/app/endpoints
```

Adiciona manualmente uma nova monitorização, que é adicionada aos dados já existentes:
```shell script
curl -i \
     --header "Content-Type: application/json" \
     --data '{"service":"app","latitude":"39.575097","longitude":"-8.909794","count":"1"}' \
     http://localhost:2525/api/metrics
```

## Ferramentas

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The Collaboration Platform for API Development

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore/detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP nicely when you visit it 'directly' in a browser tab

[<img src="https://i.imgur.com/LvZ3Anc.png" alt="" width="48" height="48"> Golang playground](https://play.golang.org/) - Go Playground is a web service that runs on golang.org's servers

## Licença

Registration-client está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.