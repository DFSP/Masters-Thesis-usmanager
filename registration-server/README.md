# Registration Server

Registration server é um modulo spring boot, gerido com maven, que inclui um [Servidor Eureka](https://github.com/Netflix/eureka) 
para registar e descobrir microserviços.

Foi usado o [spring Initializr](https://start.spring.io/) para gerar o código necessário.

### Utilização

Sem argumentos  
`mvn spring-boot:run`

Com argumentos  
`mvn spring-boot:run -Dspring-boot.run.arguments="--port=8761 --host=localhost --ip=localhost 
--id=eureka-server_localhost_8761 --zone=http://localhost:8761/eureka"`

Usando o jar  
`mvn install`  
`java -Djava.security.egd=file:/dev/urandom -jar ./target/registration-server-0.0.1.jar --port=8761 --host=localhost 
--ip=localhost --id=eureka-server_localhost_8761 --zone=http://localhost:8761/eureka`

### Guias
[Spring Eureka Server](https://spring.io/guides/gs/service-registration-and-discovery) - This guide walks you through the process of starting and using the Netflix Eureka service registry.
