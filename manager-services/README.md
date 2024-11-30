# Manager services

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Module that contains all services common to [master](../manager-master) and [worker](../manager-worker) managers.

## Requirements

##### Maven with java 11
```shell script
sudo apt install maven
mvn --version
```
Confirm that it is associated with java 11 ([solution](https://stackoverflow.com/a/49988988)).

## Install

```shell script
mvn clean install -DskipTests -U
```

##Docker
```shell script
docker build -f src/main/docker/Dockerfile . -t manager-services
```

## Include the dependency
##### Maven
```xml
<dependency>
 <groupId>en.unl.fct.miei.usmanagement.manager</groupId>
 <artifactId>manager-services</artifactId>
 <version>0.0.1</version>
 <scope>compile</scope>
</dependency>
```

## Tools

[<img src="https://i.imgur.com/cIrt8pC.png" alt="" width="48" height="48"> kafkacat](https://github.com/edenhill/kafkacat) - kafkacat is a generic non-JVM producer and consumer for Apache Kafka >=0.8

## License

Manager services is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
