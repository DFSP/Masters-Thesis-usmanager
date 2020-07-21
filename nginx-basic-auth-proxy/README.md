# Nginx Proxy with Basic Auth

[![Docker Repository on Quay](https://quay.io/repository/dtan4/nginx-basic-auth-proxy/status "Docker Repository on Quay")](https://quay.io/repository/dtan4/nginx-basic-auth-proxy)

Simples [*Proxy HTTP*](https://developer.mozilla.org/docs/Web/HTTP/Proxy_servers_and_tunneling) com [*Basic Authentication*](https://developer.mozilla.org/docs/Web/HTTP/Authentication).

```
       w/ user:pass   +------------------------+      +-------------+
User ---------------> | nginx-basic-auth-proxy | ---> | HTTP Server |
                      +------------------------+      +-------------+
```

#### Docker

```bash
$ docker run \
    --rm \
    --name nginx-basic-auth-proxy \
    -p 8080:80 \
    -e BASIC_AUTH_USERNAME=username \
    -e BASIC_AUTH_PASSWORD=password \
    -e PROXY_PASS=https://www.google.com \
    -e SERVER_NAME=proxy.dtan4.net \
    -e PORT=80 \
    quay.io/dtan4/nginx-basic-auth-proxy
```

Aceder a http://localhost:8080 , e introduzir a *password* e *username*.

### Endpoints para monitorização

`:8080/nginx_status` devolve as métricas do Nginx.

```sh-session
$ curl localhost:8080/nginx_status
Active connections: 1
server accepts handled requests
 8 8 8
Reading: 0 Writing: 1 Waiting: 0
```

### Variáveis de ambiente (*Environment variables*)

##### Obrigatórias

|Key|Description|
|---|---|
|`BASIC_AUTH_USERNAME`|Basic auth username|
|`BASIC_AUTH_PASSWORD`|Basic auth password|
|`PROXY_PASS`|Proxy destination URL|

##### Opcionais

|Key|Description|Default|
|---|---|---|
|`SERVER_NAME`|Value for `server_name` directive|`example.com`|
|`PORT`|Value for `listen` directive|`80`|
|`CLIENT_MAX_BODY_SIZE`|Value for `client_max_body_size` directive|`1m`|
|`PROXY_READ_TIMEOUT`|Value for `proxy_read_timeout` directive|`60s`|
|`WORKER_PROCESSES`|Value for `worker_processes` directive|`auto`|

### Autor

Daisuke Fujita ([@dtan4](https://github.com/dtan4))

### Licença

[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE)
