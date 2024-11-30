# Manager database

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Module that contains all entities and repositories that make up the component database
[master](../manager-master) and [worker](../manager-worker) managers.

## Requirements

#### Maven with java 11
```shell script
sudo apt install maven`
mvn --version
```
Confirm that it is associated with java 11 ([solution](https://stackoverflow.com/a/49988988)).

 ## Install

```shell script
mvn clean install -DskipTests -U
```

##Docker
```shell script
docker build -f src/main/docker/Dockerfile . -t manager-database
```

## Include the dependency
##### Maven
```xml
<dependency>
 <groupId>en.unl.fct.miei.usmanagement.manager</groupId>
 <artifactId>manager-database</artifactId>
 <version>0.0.1</version>
 <scope>compile</scope>
</dependency>
```

## License

Manager database is licensed with [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
