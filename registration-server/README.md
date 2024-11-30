# RegistrationServer

Registration server is a spring boot module, managed with maven, which includes a [Eureka Server](https://github.com/Netflix/eureka)
to register and discover microservices.

## Usage

<sup>Note: 30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 is the sha1 of `eureka-server_127.0.0.1_8761`</sup>

#### With Maven

Using the spring-boot plugin
```
mvn spring-boot:run -Dspring-boot.run.arguments="--port=8761 --host=127.0.0.1 --ip=127.0.0.1
--id=30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 --zone=http://127.0.0.1:8761/eureka"
```

Using jar
```
clean mvn package -DskipTests
java -Djava.security.egd=file:/dev/urandom -jar target/registration-server.jar \
--port=8761 --host=127.0.0.1 --ip=127.0.0.1 --id=30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 --zone=http://127.0.0.1:8761/eureka
```

#### With Docker

```
docker build -f docker/Dockerfile . -t registration-server
docker run --rm -p 8761:8761 registration-server
```

## Guides
[Spring Eureka Server](https://spring.io/guides/gs/service-registration-and-discovery) - This guide walks you through the process of starting up and using the Netflix Eureka service registration.

## Tools

## License

Registration-server is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
