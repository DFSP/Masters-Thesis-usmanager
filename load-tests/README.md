# Load-tests

Load tests carried out on system components.

## Services

### Usage

SERVICE_ADDRESS is the address of the service to which the load tests will be carried out, which can be located on the edge of the network or in a virtual machine in the cloud.

##### Locally
```shell script
k6 run -e SERVICE_ADDRESS=hostname:port sockShopCatalogue.js
```

##### Locally but with results sent to the cloud
```shell script
k6 login cloud -t [key]
k6 run -o cloud -e SERVICE_ADDRESS=hostname:port sockShopCatalogue.js
```

##### In the cloud
```shell script
k6 login cloud -t [key]
k6 cloud -e SERVICE_ADDRESS=hostname:port sockShopCatalogue.js
```

## Execution times

METHOD = HEAD | DELETE | POST | GET | OPTIONS | PATCH | PUT
URL is the url relative to the path to be executed:
http://localhost:8080/api/{URL}
HOST_ADDRESS is optional, indicates the host address of the manager. If not present, localhost is used.
ITERATIONS is optional, it indicates the number of executions to be made. If not present, 1 iteration is performed.

Examples:

Starting a sock-shop-catalogue service container.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=containers \
 -e REQUEST_BODY='{"service":"sock-shop-catalogue","externalPort":8083,"internalPort":80,"hostAddress":{"username":"daniel","publicIpAddress":"192.168. 1.93","privateIpAddress":"192.168.1.93","coordinates":{"label":"Portugal","latitude":39.575097,"longitude":-8.909794}}}' \
 executionTimes.js
```

Starting a log server.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=registration-servers \
 -e REQUEST_BODY='{"regions":["Europe"]}' \
 executionTimes.js
```
Starting a load balancer.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=load-balancers\
 -e REQUEST_BODY='{"regions":["Europe"]}' \
 executionTimes.js
```

Starting a local manager.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=worker-managers\
 -e REQUEST_BODY='{"regions":["Europe"]}' \
 executionTimes.js
```

Starting a kafka agent.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=kafka \
 -e REQUEST_BODY='{"regions":["Europe"]}' \
 executionTimes.js
```

Configure a cloud instance.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=hosts/cloud \
 -e REQUEST_BODY='{"coordinates":{"label":"France","title":"France","longitude":-0.5001341691786153,"latitude":46.734370418273606}}' \
 executionTimes.js
```

Terminate a cloud instance.
```shell script
k6 run \
 -e METHOD=DELETE \
 -e URL=hosts/cloud/i-07e7aaec6d82292a0 \
 executionTimes.js
```

Sync virtual instances in AWS with the main manager database.
```shell script
k6 run \
 -e METHOD=POST \
 -e URL=hosts/cloud/sync \
 executionTimes.js
```
