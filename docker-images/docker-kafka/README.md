# Containerized kafka with zookeeper

Docker container to start kafka and zookeeper processes.  
Based on [spotify kafka image](https://hub.docker.com/r/spotify/kafka).

## Run

```shell script
export KAFKA_HOSTNAME=127.0.0.1
export KAFKA_PORT=9092
docker run -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=$KAFKA_HOSTNAME --env ADVERTISED_PORT=$KAFKA_PORT kafka
```