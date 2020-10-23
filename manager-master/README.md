# Manager master   

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Master manager é o módulo principal do sistema de gestão de micro-serviços.
Faz uso de conhecimento prévio (serviços e sua dependencias, regras, tipo de decisões, etc) e de conhecimento que adquire ao longo da sua execução (métricas obtidas dos containers e hosts, migração/replicação de serviços, etc).  
Usa o sistema de gestão de regras de negócios [Drools](https://www.drools.org/) para gerir as regras aplicadas. 
É um módulo [Spring Boot](https://spring.io/projects/spring-boot), gerido com [maven](https://maven.apache.org/), que disponibiliza um servidor restful.

## Requisitos

#### Maven com java 11  
```shell script
sudo apt install maven
maven --version
```
Confirmar que está associado ao java 11 ([solução](https://stackoverflow.com/a/49988988)).
 
#### SSH
```shell script
sudo apt-get install ssh
```

#### Docker
```shell script
sh src/main/resources/scripts/docker-install.sh
```

#### Node Exporter
```shell script
sh src/main/resources/scripts/node-exporter-install.sh
``` 

## Executar

#### Local
```shell script
mvn spring-boot:run
```

#### Docker

##### Local
```shell script
docker build -f docker/Dockerfile . -t manager-master
docker run --rm -p 8080:8080 manager-master
```

##### Hub
##### Hub
```shell script
docker run --rm -p 8080:8080 usmanager/manager-master
```

## Ambiente

> Ubuntu 20.04.1 LTS

> Apache Maven 3.6.0  
  Maven home: /usr/share/maven  
  Java version: 11.0.6, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64  
  Default locale: pt_PT, platform encoding: UTF-8  
  OS name: "linux", version: "5.3.0-28-generic", arch: "amd64", family: "unix"

> Docker version 19.03.13, build 4484c46d9d

## Cloud

Lançar a aplicação na cloud (aws):
- https://aws.amazon.com/pt/blogs/devops/deploying-a-spring-boot-application-on-aws-using-aws-elastic-beanstalk/

## Ferramentas

[<img src="https://i.imgur.com/71OViyN.png" alt="" width="48" height="62"> Drools](https://www.drools.org/) - Drools is a Business OldRules Management System (BRMS) solution

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The Collaboration Platform for API Development

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore/detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP nicely when you visit it 'directly' in a browser tab

[<img src="https://i.imgur.com/JCWN9oL.png" alt="" width="48" height="48"> Project Lombok](https://projectlombok.org/) - Project Lombok is a java library that automatically plugs into your editor and build tools, spicing up your java

[<img src="https://i.imgur.com/6f2iyaR.png" alt="" width="48" height="24"> Checkstyle](https://checkstyle.org/) - Checkstyle is a development tool to help programmers write Java code that adheres to a coding standard

[<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/SSH_Communications_Security_logo.svg/1280px-SSH_Communications_Security_logo.svg.png" alt="" alt="" width="128" height="20">](https://www.ssh.com/ssh/command) - Tools for remote access

## Guias úteis
[<img src="https://i.imgur.com/WDbhA08.png" alt="" width="48" height="42"> Spring Boot](https://spring.io/projects/spring-boot) - Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run" 

<!--[<img src="https://i.imgur.com/ei7nKF5.png" alt="" width="48" height="42"> Spring HATEOAS](https://spring.io/projects/spring-hateoas) - Spring HATEOAS provides some APIs to ease creating REST representations that follow the HATEOAS principle when working with Spring and especially Spring MVC-->

[<img src="https://i.imgur.com/qFZtEoa.png" alt="" width="48" height="24"> Maven](http://maven.apache.org/guides/getting-started/) - Maven is essentially a project management and comprehension tool and as such provides a way to help with managing: Builds, Documentation, Reporting, Dependencies, SCMs, Release, Distribution and Documentation


## Troubleshooting

- Got permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock:
Configurar  encaminhamento `sudo setfacl --modify user:<user name or ID>:rw /var/run/docker.sock` ([solução](https://stackoverflow.com/a/54504083))

- Se ao estabeler uma ligação com o aws ec2, bloquear, fazer o seguinte:
https://stackoverflow.com/a/48572280 

- Utilizar computadores pessoais:  
Editar o ficheiro sudoers usando o comando `sudo visudo` e adicionar no final do ficheiro `user ALL=(ALL) NOPASSWD: ALL`, 
substituindo user pelo username da conta a usar. Isto permite executar comandos sudo sem ser pedida a password.  
Normalmente protegidos pelo router (http://192.168.1.254 no caso de *meo*, 
http://192.168.1.1 no caso de *nos* ou *vodafone*), estão apenas acessíveis na rede local.  
Se o router usar [DHCP](https://pt.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol) para atribuir ips dinâmicos 
às máquinas, é preciso definir um ip estático para o host desejado, através da interface do router.  
E por fim, configurar os seguintes encaminhamentos de portas no painel de controlo do router: 
    - Ssh, porta 22 TCP. Aceder usando `ssh user@ip_publico_do_router` ([ver ip público](https://ipinfo.io/ip))
    - Docker Cluster management communications, porta 2377 TCP
    - Communication among docker nodes/Container network discovery, porta 7946 TCP e UDP 
    - Docker Overlay network traffic/Container ingress network, porta 4789 UDP
    - Prometheus, porta 9090 TCP
    
De notar que, apenas com esta configuração, não será possível executar containers aplicacionais nesta máquina.

## Licença

Master manager está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.