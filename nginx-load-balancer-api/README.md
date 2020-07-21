# Nginx load balancer API
API to add new servers to Nginx load balancer and generate new config files

#### Executar

```
cd nginx-load-balancer-api/cmd
go build -o nginx-load-balancer-api
./nginx-load-balancer-api
```

O resultado é o ficheiro binário `nginx-load-balancer-api`, gerado na diretoria atual.

### API Endpoints

Os URIs são relativos a *http://localhost:1906/_/nginx-load-balancer-api/api*

HTTP request | Description
------------ | -------------
**Get** /servers | List current servers
**POST** /servers | Add new servers. Request example : [{"hostname" : "server1:8080"}]
**DELETE** /servers | Deletes a server. Request example : {"hostname" : "server1:8080"} <!---TODO fix delete-->


## License

Nginx-load-balancer-api está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.