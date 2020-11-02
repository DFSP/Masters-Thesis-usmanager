# Manager services

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Módulo que contém todos os serviços comuns aos [master](../manager-master) e [worker](../manager-worker) managers.

## Requisitos

##### Maven com java 11  
```shell script
sudo apt install maven  
mvn --version
```
Confirmar que está associado ao java 11 ([solução](https://stackoverflow.com/a/49988988)).
 
## Instalar
 
```shell script
mvn clean install -DskipTests -U
```

##  Docker
```shell script
docker build -f src/main/docker/Dockerfile . -t manager-services
```

## Incluir a dependência  
##### Maven
```xml
<dependency>
    <groupId>pt.unl.fct.miei.usmanagement.manager</groupId>
    <artifactId>manager-services</artifactId>
    <version>0.0.1</version>
    <scope>compile</scope>
</dependency>
```

## Licença

Manager services está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.