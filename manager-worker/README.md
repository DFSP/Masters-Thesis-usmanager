# Manager worker

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Manager who makes the connection between edge and cloud machines.
It is designed to run on machines located between the edge and the cloud.
It manages a set of nodes and containers at the edge.
It is a java module managed with maven, uses the spring-boot framework.

## Dependencies

- [Spring-boot](https://spring.io/projects/spring-boot) - Spring Boot makes it easy to create standalone, production-grade Spring-based applications that you can "just run"
- [JDBC](https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/) - The Java Database Connectivity (JDBC) API provides universal data access from the Java programming language. Using the JDBC API, you can access virtually any data source, from relational databases to spreadsheets and flat sheets
- [Lombok](https://projectlombok.org/) - Project Lombok is a java library that automatically plugs into your editor and build tools, spying on your java
- [Spotify docker-client](https://github.com/spotify/docker-client) - Docker client written in Java
- [sshj](https://github.com/hierynomus/sshj) - SSHv2 Library for Java
- [SymmetricDS](https://www.symmetricds.org/) - SymmetricDS is open source database replication software that focuses on cross-platform features and compatibility
## To execute

##### Arguments
- ID - manager id, must be unique, so that it can be differentiated from other local managers.
- HOST_ADDRESS - address associated with the manager, in json format, [see structure](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/hosts/HostAddress.java).
- HOST_ADDRESS - address associated with the manager, in json format, [see structure](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/hosts/HostAddress.java).

<sup>Change argument values ​​as needed:</sup>

#### Location

```shell script
export ID=001
export HOST_ADDRESS='{"username":"...","publicDnsName":"...","publicIpAddress":"...","privateIpAddress":"...","coordinates":{ "label":"Portugal","latitude":...,"longitude":...},"region":"EUROPE","place":"..."}'
mvn spring-boot:run
```

####Docker
```shell script
docker build -f ../manager-worker/src/main/docker/Dockerfile .. -t manager-worker
docker run --rm -p 8081:8081 -v /var/run/docker.sock:/var/run/docker.sock -e EXTERNAL_ID=... -e HOST_ADDRESS=... -e REGISTRATION_URL=... manager-worker
```

##### Hub
```shell script
docker run --rm -p 8081:8081 -e -v /var/run/docker.sock:/var/run/docker.sock EXTERNAL_ID=... -e HOST_ADDRESS=... -e REGISTRATION_URL=... usmanager/manager-worker
```

## License

Worker manager is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
