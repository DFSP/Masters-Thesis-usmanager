# Containerized kafka with zookeeper

Based on [spotify kafka image](https://hub.docker.com/r/spotify/kafka).

## Run

```shell script
kafka_hostname=127.0.0.1
kafka_port=9092
docker run -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=$kafka_hostname --env ADVERTISED_PORT=$kafka_port kafka
```