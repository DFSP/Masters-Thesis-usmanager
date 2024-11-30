# Nginx Proxy with Basic Authentication

[![Docker Repository on Quay](https://quay.io/repository/dtan4/nginx-basic-auth-proxy/status "Docker Repository on Quay")](https://quay.io/repository/dtan4 /nginx-basic-auth-proxy)

Simple [*HTTP Proxy*](https://developer.mozilla.org/docs/Web/HTTP/Proxy_servers_and_tunneling) with [*Basic Authentication*](https://developer.mozilla.org/docs/Web/HTTP/ authentication).

```
 w/ user:pass +--------------------------+ +-------------+
User ---------------> | nginx-basic-auth-proxy | ---> | HTTP Server |
 +-----------------------+ +-------------+
```

####Docker

```bash
$dockerrun\
 --itd\
 --name nginx-basic-auth-proxy \
 -p 8080:80 \
 --rm\
 -e BASIC_AUTH_USERNAME=username \
 -e BASIC_AUTH_PASSWORD=password \
 -e PROXY_PASS=https://$PRIVATE_IP:2375 \
 usmanager/nginx-basic-auth-proxy
```

Access http://$PRIVATE_IP:2375 , and enter *password* and *username*.

### Endpoints for monitoring

`:2375/nginx_status` returns Nginx metrics.

```sh-session
$ curl $PRIVATE_IP:2375/nginx_status
Active connections: 1
server accepts raised requests
 8 8 8
Reading: 0 Writing: 1 Waiting: 0
```

### Environment variables (*Environment variables*)

##### Mandatory

|Key|Description|
|---|---|
|`BASIC_AUTH_USERNAME`|Basic auth username|
|`BASIC_AUTH_PASSWORD`|Basic authentication password|
|`PROXY_PASS`|Proxy destination URL|

##### Optional

|Key|Description|Default|
|---|---|---|
|`SERVER_NAME`|Value for `server_name` directive|`example.com`|
|`PORT`|Value for `listen` directive|`80`|
|`CLIENT_MAX_BODY_SIZE`|Value for `client_max_body_size` directive|`1m`|
|`PROXY_READ_TIMEOUT`|Value for `proxy_read_timeout` directive|`60s`|
|`WORKER_PROCESSES`|Value for `worker_processes` directive|`auto`|

### Author

Daisuke Fujita ([@dtan4](https://github.com/dtan4))

### License

[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE)
