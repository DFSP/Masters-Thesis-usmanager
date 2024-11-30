# Nginx load balancer API

API to add or remove servers to [Nginx load balancer](../nginx-load-balancer) and update configuration files.

## Arguments

Usage of ./nginx-load-balancer-api:

- delay(int)

 Update delay (in seconds) of nginx configuration after adding a new server (default 15)

- port (int)

 Port to connect HTTP listener (default 1906)

nginx-load-balancer-api can be initialized with servers, through an environment variable in json format, e.g.:

```json
[
 {
 "service": "app",
 "servers": [
 {
 "server": "202.193.200.125:5000",
 "latitude": 39.575097,
 "longitude": -8.909794,
 "region": "EUROPE"
 },
 {
 "server": "202.193.20.125:5000",
 "latitude": 39.575097,
 "longitude": -8.909794,
 "region": "EUROPE"
 }
 ]
 },
 {
 "service": "app2",
 "servers": [
 {
 "server": "202.193.203.125:5000",
 "latitude": 39.575097,
 "longitude": -8.909794,
 "region": "EUROPE"
 }
 ]
 }
]
```

## To execute

#### Location

```shell script
sudo nginx
go build -o nginx-load-balancer-api
sudo ./nginx-load-balancer-api
```
With home servers
```shell script
sudo env SERVERS="[{\"service\":\"app1\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097 ,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\" longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\ "202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
./nginx-load-balancer-api
```

####Docker

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer-api
docker run -p 1906:1906 nginx-load-balancer-api
```

With home servers

```shell script
docker build -f docker/Dockerfile . -t nginx-load-balancer-api
docker run -p 1906:1906 \
-e SERVERS="[{\"service\":\"app1\",\"servers\":[{\"server\":\"202.193.200.125:5000\",\"latitude\":39.575097 ,\"longitude\":-8.909794,\"region\":\"EUROPE\"},{\"server\":\"202.193.20.125:5000\",\"latitude\":39.575097,\" longitude\":-8.909794,\"region\":\"EUROPE\"}]},{\"service\":\"app2\",\"servers\":[{\"server\":\ "202.193.203.125:5000\",\"latitude\":39.575097,\"longitude\":-8.909794,\"region\":\"EUROPE\"}]}]" \
nginx-load-balancer-api
```

## API endpoints

URIs are relative to *http://localhost:1906/api*

HTTP Request | Description
------------ | -------------
**Get** /servers | Get the servers of all services registered to this load balancer
**Get** /{service}/servers | Lists all servers of the service `{service}` registered to this load balancer
**POST** /{service}/servers | Adds new servers to the `{service}` service. Request: `[{server, latitude, longitude, region}]`
**DELETE** /{service}/servers/{server} | Removes server `{server}` from service `{service}`

## Examples

Get the servers for all services:
```shell script
curl -i --user username:password http://localhost:1906/api/servers
```

Get the servers for service `app1`:
```shell script
curl -i --user username:password http://localhost:1906/api/app1/servers
```

Adding a server to the `app1` service:
```shell script
curl -i \
 --user username:password \
 --header "Content-Type: application/json" \
 --data '[{"server":"202.193.200.125:5000","latitude":39.575097,"longitude":-8.909794,"region":"EUROPE"}]' \
 http://localhost:1906/api/app1/servers
```

Remove server `202.193.200.125:5000` from service `app1`:
```shell script
curl -i \
 --user username:password \
 -X DELETE \
 http://localhost:1906/api/app1/servers/202.193.200.125:5000
```

## License

Nginx-load-balancer-api is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
