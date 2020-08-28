# Manager worker

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Gestor que faz a conexão entre as máquinas edge e cloud.
Está desenhado para ser executado em máquinas localizadas entre a edge e a cloud.  
Gere um conjunto de nós e containers na edge.  
Usa [kafka](https://kafka.apache.org/) para comunicar com o manager-master.  
É um módulo java gerido com maven, usa a framework spring-boot.

## Dependências

- [Spring-boot](https://spring.io/projects/spring-boot) - Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run"
- [JDBC](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/) - The Java Database Connectivity (JDBC) API provides universal data access from the Java programming language. Using the JDBC API, you can access virtually any data source, from relational databases to spreadsheets and flat files
- [Lombok](https://projectlombok.org/) - Project Lombok is a java library that automatically plugs into your editor and build tools, spicing up your java
- [Spotify docker-client](https://github.com/spotify/docker-client) - Docker client written in Java
- [sshj](https://github.com/hierynomus/sshj) - SSHv2 library for Java 
- [SymmetricDS](https://www.symmetricds.org/) - SymmetricDS is open source database replication software that focuses on features and cross platform compatibility
## Executar

##### Argumentos
- id - id do worker-manager, deve ser único, para que seja diferenciado dos outros worker-managers
- master - hostname do master-manager 

<sup>Alterar os valores dos argumentos, conforme necessário:</sup>

#### Local
```shell script
mvn spring-boot:run -Dspring-boot.run.arguments="--id=001 --master=127.0.0.1"
```
ou
```shell script
export ID=001 MASTER=127.0.0.1
mvn spring-boot:run
```

#### Docker

##### Local
```shell script
docker build -f docker/Dockerfile . -t manager-worker
docker run --rm -p 8081:8081 -e id=worker-1 -e master=127.0.0.1 manager-worker
```

##### Hub
```shell script
docker run --rm -p 8081:8081 -e id=worker-1 -e master=127.0.0.1 usmanager/manager-worker
```