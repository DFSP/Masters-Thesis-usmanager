# Manager database

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Módulo que contém todas as entidades e repositórios que constituem a base de dados dos componentes 
[master](../manager-master) e [worker](../manager-worker) managers.

## Requisitos

#### Maven com java 11  
```shell script
sudo apt install maven`  
maven --version
```
Confirmar que está associado ao java 11 ([solução](https://stackoverflow.com/a/49988988)).
 
 ## Executar   
 
```shell script
mvn clean install -DskipTests -U
```

## Incluir a dependência 
##### Maven
```xml
<dependency>
    <groupId>pt.unl.fct.usmanagement.manager</groupId>
    <artifactId>manager-database</artifactId>
    <version>0.0.1</version>
    <scope>compile</scope>
</dependency>
```

## Licença

Manager database está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.