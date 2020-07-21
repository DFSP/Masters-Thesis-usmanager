# Registration-client

Regista um serviço no [Servidor Eureka](../registration-server), e obtém o endpoints dos outros serviços, registados no servidor.

#### Executar

```shell script
cd cmd
go build -o registration-client
./registration-client
```

O resultado é o ficheiro binário `registration-client`, gerado na diretoria atual.

### API Endpoints

Os URIs são relativos a *http://localhost:1906/api/apps*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AppsApi* | **GetAllAppsByName** | **Get** /{appName}/all | Get all apps endpoints by app name
*AppsApi* | **GetAppsByName** | **Get** /{appName} | Get an app endpoint by app name

## License

Registration-client está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.