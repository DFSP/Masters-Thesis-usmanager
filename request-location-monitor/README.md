# Request location monitor

Saves the number of requests for a service, with location details.

## Arguments

Usage of ./request-location-monitor:

- port (int)

 Port to connect HTTP listener (default 1919)

-interval(int)

 Default interval (in ms) to include instances in data aggregation (default 60000)

## To execute

#### Location
```shell script
go build -o location-monitor-request
./request-location-monitor
```

####Docker

```shell script
docker build -f docker/Dockerfile . -t request-location-monitor
docker run --rm -p 1919:1919 request location monitor
```

## API endpoints

URIs are relative to *http://localhost:1919/api*

HTTP Request | Description
------------ | -------------
**Get** /monitoring | List all registered monitoring
**Get** /monitoring?aggregation | Lists all recorded monitoring aggregated by service, in the last 60 seconds
**Get** /monitoring?aggregation&interval={ms} | Lists all recorded monitoring aggregated by service, in the last {ms} milliseconds
**Post** /monitoring | Adds a new monitoring: `{service, latitude, longitude, count}`

## Example

View all monitoring:
```shell script
curl -i http://localhost:1919/api/monitoring
```

See all monitoring, in the last 60 seconds, aggregated by service:
```shell script
curl -i http://localhost:1919/api/monitoring?aggregation
```

See all monitoring, in the last 120 seconds, aggregated by service:
```shell script
curl -i http://localhost:1919/api/monitoring?aggregation&interval=120000
```

Registers a new monitoring of a service:
```shell script
curl -i \
 --header "Content-Type: application/json" \
 --data '[{"service":"app","latitude":39.575097,"longitude":-8.909794,"count":1}]' \
 http://localhost:1919/api/location/requests
```

## Tools

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The API Development Collaboration Platform

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore /detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP actually when you visit 'directly' in a browser tab

[<img src="https://i.imgur.com/LvZ3Anc.png" alt="" width="48" height="48"> Golang playground](https://play.golang.org/) - Go Playground is a web service that runs on the servers of golang.org

## License

Request-location-monitor is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
