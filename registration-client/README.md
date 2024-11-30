# Registration-client

Register the service on the [Eureka Server](../registration-server), and get the endpoints of other services, also registered on the server.
It uses the distances between endpoints in the choice algorithm.

## Arguments

Usage of ./registration-client:

 - service (string) *

 Service name

 - latitude (float) *

 Latitude Service

 - longitude (float) *

 Service Longitude

 - hostname (string)

 Service Hostname (default "localhost")

 - port (int) *

 Service Port

 - process (string)

 Process name to monitor

 - cache-time (int)

 Time (in ms) to cache endpoint instances before Eureka contact (default 10000)

 - server (string)

 Registration server (default "127.0.0.1:8761")

 -interval(int)

 Interval time (in ms) to send location data (default 5000)

 -register(bool)

 True: registration-client will register service on Eureka; False: service must manually trigger the register (default true)

 - register-port (int)

 Port to start http server

## To execute

### Location

```shell script
go build -o registration-client
./registration-client -service app -latitude 38.660758 -longitude -9.203568
```

The result is the binary file `registration-client`, generated in the current directory.

###Docker

```shell script
docker build -f docker/Dockerfile . -t registration-client
docker run -p 1906:1906 -e service=app -e latitude=38.660758 -e longitude=-9.203568 -e eureka={publicIp}:8761 registration-client
```

## API endpoints

URIs are relative to *http://localhost:1906/api*

HTTP Request | Description
------------ | -------------
**Post** /register | Register the service on the eureka server
**Get** /services/{service}/endpoint | Gets the closest endpoint to the service `{service}`
**Get** /services/{service}/endpoint?among=x | Gets a random endpoint for the service `{service}` among the x closest services
**Get** /services/{service}/endpoint?range=d | Gets the best endpoint for the service `{service}` starting the search at a distance of d kilometers
**Get** /services/{service}/endpoints | Gets all endpoints registered to the service name `{service}`
**Post** /metrics | Adds new monitoring for this endpoint. Request body: `{service, latitude, longitude, count}`

## Example

Manually register the endpoint:
```shell script
curl -i -X ​​POST http://localhost:1906/api/register
```

Gets the best endpoint for the application service
```shell script
curl -i http://localhost:1906/services/app/endpoint
```

Gets all app service endpoints
```shell script
curl -i http://localhost:1906/services/app/endpoints
```

Manually add new monitoring, which is added to existing data:
```shell script
curl -i \
 --header "Content-Type: application/json" \
 --data '{"service":"app","latitude":39.575097,"longitude":-8.909794,"count":1}' \
 http://localhost:1906/api/metrics
```

## Tools

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The API Development Collaboration Platform

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore /detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP actually when you visit 'directly' in a browser tab

[<img src="https://i.imgur.com/LvZ3Anc.png" alt="" width="48" height="48"> Golang playground](https://play.golang.org/) - Go Playground is a web service that runs on the servers of golang.org

## License

Registration-client is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
