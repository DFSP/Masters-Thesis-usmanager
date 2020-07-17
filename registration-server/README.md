# Registration Server

Registration server é um modulo spring boot, gerido com maven, que inclui um [Servidor Eureka](https://github.com/Netflix/eureka) 
para registar e descobrir microserviços.

Foi usado o [spring Initializr](https://start.spring.io/) para gerar o código necessário.

### Utilização

Sem argumentos  
`mvn spring-boot:run`

Com argumentos  
`mvn spring-boot:run -Dspring-boot.run.arguments="8761,8761,localhost`

### Guias
[Spring Eureka Server](https://spring.io/guides/gs/service-registration-and-discovery) - This guide walks you through the process of starting and using the Netflix Eureka service registry.
