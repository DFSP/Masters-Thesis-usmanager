# Manager worker

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Gestor que faz a conexão entre as máquinas edge e cloud.
Está desenhado para ser executado em máquinas localizadas entre a edge e a cloud.  
Gere um conjunto de nós e containers na edge.  
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
- ID - id do gestor, deve ser único, para que seja diferenciado dos outros gestores locais.
- HOST_ADDRESS - endereço associado ao gestor, em formato json, [ver estrutura](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/hosts/HostAddress.java).
- HOST_ADDRESS - endereço associado ao gestor, em formato json, [ver estrutura](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/hosts/HostAddress.java).

<sup>Alterar os valores dos argumentos, conforme necessário:</sup>

#### Local

```shell script
export ID=001 
export HOST_ADDRESS='{"username":"...","publicDnsName":"...","publicIpAddress":"...","privateIpAddress":"...","coordinates":{"label":"Portugal","latitude":...,"longitude":...},"region":"EUROPE","place":"..."}'
mvn spring-boot:run
```

#### Docker
```shell script
docker build -f ../manager-worker/src/main/docker/Dockerfile .. -t manager-worker
docker run --rm -p 8081:8081 -v /var/run/docker.sock:/var/run/docker.sock -e ID=... -e HOST_ADDRESS=... -e KAFKA_BOOTSTRAP_SERVERS=... manager-worker
```

##### Hub
```shell script
docker run --rm -p 8081:8081 -e -v /var/run/docker.sock:/var/run/docker.sock ID=... -e HOST_ADDRESS=... -e KAFKA_BOOTSTRAP_SERVERS=... usmanager/manager-worker
```

## Licença

Worker manager está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.