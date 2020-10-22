# Registration Server

Registration server é um modulo spring boot, gerido com maven, que inclui um [Servidor Eureka](https://github.com/Netflix/eureka) 
para registar e descobrir microserviços.

## Utilização

<sup>Nota: 30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 é o sha1 de `eureka-server_127.0.0.1_8761`</sup>

#### Com Maven

Usando o plugin spring-boot  
```
mvn spring-boot:run -Dspring-boot.run.arguments="--port=8761 --host=127.0.0.1 --ip=127.0.0.1
--id=30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 --zone=http://127.0.0.1:8761/eureka"
```

Usando o jar  
```
mvn clean package -DskipTests
java -Djava.security.egd=file:/dev/urandom -jar target/registration-server.jar \
--port=8761 --host=127.0.0.1 --ip=127.0.0.1 --id=30b5f1e3d8cb54b548dae75d3a97e6a7f0657257 --zone=http://127.0.0.1:8761/eureka
```

#### Com Docker

```
docker build -f docker/Dockerfile . -t registration-server
docker run --rm -p 8761:8761 registration-server
```

## Guias
[Spring Eureka Server](https://spring.io/guides/gs/service-registration-and-discovery) - This guide walks you through the process of starting and using the Netflix Eureka service registry.

## License

Registration-server está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.