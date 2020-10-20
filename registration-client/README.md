# Registration-client

Regista o serviço no [Servidor Eureka](../registration-server), e obtém o endpoints dos outros serviços, também registados no servidor.

## Executar

#### Local

```shell script
cd cmd
go build -o registration-client
./registration-client
```
O resultado é o ficheiro binário `registration-client`, gerado na diretoria atual.

#### Docker

```shell script
docker build -f docker/Dockerfile . -t registration-client
docker run -p 1906:1906 registration-client
```

## API Endpoints

Os URIs são relativos a *http://localhost:1906/api*

HTTP request | Description
------------ | -------------
**Post** /api/register | Regista o endpoint no servidor eureka
**Get** /services/{service}/endpoint | Obtém o melhor endpoint para o serviço {service}
**Get** /services/{service}/endpoints | Obtém todos os endpoints registados em nome do serviço {service}
**Post** /api/metrics | Adiciona uma nova monitorização deste endpoint


## Licença

Registration-client está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.