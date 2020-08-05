# Manager worker

Gestor que faz a conexão entre as máquinas edge e cloud.
Está desenhado para ser executado em máquinas localizadas entre a edge e a cloud.  
Gere um conjunto de nós e containers na edge.  
Usa [kafka](https://kafka.apache.org/) para comunicar com o manager-master.  
É um módulo java gerido com maven, usa a framework spring-boot.

## Dependências

- [Spring-boot](https://spring.io/projects/spring-boot) - Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run"
- [JDBC](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/) - The Java Database Connectivity (JDBC) API provides universal data access from the Java programming language. Using the JDBC API, you can access virtually any data source, from relational databases to spreadsheets and flat files
- [Kafka](https://kafka.apache.org/) - Kafka® is used for building real-time data pipelines and streaming apps
    - [Kafka Connect](https://kafka.apache.org/documentation.html#connect) - The Connect API allows implementing connectors that continually pull from some source data system into Kafka or push from Kafka into some sink data system.
        - [Kafka Connect JDBC](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc) - The JDBC source and sink connectors allow you to exchange data between relational databases and Kafka
- [Lombok](https://projectlombok.org/) - Project Lombok is a java library that automatically plugs into your editor and build tools, spicing up your java
- [Spotify docker-client](https://github.com/spotify/docker-client) - Docker client written in Java
- [sshj](https://github.com/hierynomus/sshj) - SSHv2 library for Java 
## Executar

##### Argumentos
- id - id do worker-manager, deve ser único, para que seja diferenciado dos outros worker-managers
- master - hostname do master-manager 
- hosts - lista de hostnames de máquinas que este worker-manager deve gerir

<sup>Alterar os valores dos argumentos, conforme necessário:</sup>

#### Local
```shell script
mvn spring-boot:run -Dspring-boot.run.arguments="--id=001 --master=127.0.0.1 --hosts=127.0.0.1,13.100.169.202"
```
ou
```shell script
export ID=001 MASTER=127.0.0.1 HOSTS=127.0.0.1,13.100.169.202
mvn spring-boot:run
```

#### Docker
```shell script
docker build -f docker/Dockerfile . -t manager-worker
docker run --rm -p 8080:8088 \ 
  && --env ID=worker-1 \
  && --env MASTER=127.0.0.1 \
  && --env HOSTS=127.0.0.1,13.100.169.202 manager-worker
```