# Registration-client

Regista um serviço no [Servidor Eureka](../registration-server), e obtém o endpoints dos outros serviços, registados no servidor.

## Executar

#### Local

```shell script
cd cmd
go build -o registration-client
./registration-client
```

#### Docker

```shell script
docker build -f docker/Dockerfile . -t registration-client
docker run -p 1906:1906 registration-client
```

O resultado é o ficheiro binário `registration-client`, gerado na diretoria `cmd`.

#### API Endpoints

Os URIs são relativos a *http://localhost:1906/api/apps*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AppsApi* | **GetAllAppsByName** | **Get** /{appName}/all | Get all apps endpoints by app name
*AppsApi* | **GetAppsByName** | **Get** /{appName} | Get an app endpoint by app name

#### Licença

Registration-client está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.