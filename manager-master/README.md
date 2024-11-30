# Master manager

[![js-standard-style](https://img.shields.io/badge/code%20style-checkstyle-brightgreen.svg)](https://checkstyle.org/)

Master manager is the main module of the microservices management system.
It makes use of prior knowledge (services and their dependencies, rules, types of decisions, etc.) and knowledge acquired throughout its execution (metrics obtained from containers and hosts, migration/replication of services, etc.).
It uses the [Drools](https://www.drools.org/) business rules management system to manage the applied rules.
It is a [Spring Boot](https://spring.io/projects/spring-boot) module, managed with [maven](https://maven.apache.org/), which provides a restful server.

## Dependencies

#### Maven with java 11
```shell script
sudo apt install maven
mvn --version
```
Confirm that it is associated with java 11 ([solution](https://stackoverflow.com/a/49988988)).

####SSH
```shell script
sudo apt-get install ssh
```

####Docker
```shell script
sh src/main/resources/scripts/docker-install.sh
```

#### Node Exporter
```shell script
sh src/main/resources/scripts/node-exporter-install.sh
```

## To execute

#### Location
```shell script
export ID=master-manager
mvn spring-boot:run
```

####Docker

##### Location
```shell script
docker build -f docker/Dockerfile . -t master-manager
docker run --rm -p 8080:8080 -e ID=master-manager manager-master
```

##### Docker hub
```shell script
docker run --rm -p 8080:8080 usmanager/manager-master
```

## Hal explorer

The spring boot [hal explorer](https://mvnrepository.com/artifact/org.springframework.data/spring-data-rest-hal-explorer) is activated using the url:

http://localhost:8080/api

## Environment

> Ubuntu 20.04.1 LTS

> Apache Maven 3.6.0
 Maven home: /usr/share/maven
 Java version: 11.0.6, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
 Locale default: pt_PT, platform encoding: UTF-8
 OS name: "linux", version: "5.3.0-28-generic", arch: "amd64", family: "unix"

> Docker version 19.03.13, build 4484c46d9d

## Cloud

Launch the application in the cloud (AWS):
- https://aws.amazon.com/en/blogs/devops/deploying-a-spring-boot-application-on-aws-using-aws-elastic-beanstalk/

## Tools

[<img src="https://i.imgur.com/71OViyN.png" alt="" width="48" height="62"> Drools](https://www.drools.org/) - Drools is a Business OldRules Management System (BRMS) solution

[<img src="https://i.imgur.com/DBrGTaL.png" alt="" width="48" height="48"> Postman](https://www.postman.com/) - The API Development Collaboration Platform

[<img src="https://i.imgur.com/M7dKRag.png" alt="" width="48" height="48"> Json Formatter](https://chrome.google.com/webstore /detail/json-formatter/bcjindcccaagfpapjjmafapmmgkkhgoa?hl=en) - Chrome extension for printing JSON and JSONP actually when you visit 'directly' in a browser tab

[<img src="https://i.imgur.com/JCWN9oL.png" alt="" width="48" height="48"> Project Lombok](https://projectlombok.org/) - Project Lombok is a java library that automatically plugs into your editor and build tools, spying on your java

[<img src="https://i.imgur.com/6f2iyaR.png" alt="" width="48" height="24"> Checkstyle](https://checkstyle.org/) - Checkstyle is a development tool to help programmers write Java code that adheres to a coding standard

[<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/SSH_Communications_Security_logo.svg/1280px-SSH_Communications_Security_logo.svg.png" alt="" alt="" width="128 " height="20">](https://www.ssh.com/ssh/command) - Tools for remote access

## Useful guides
[<img src="https://i.imgur.com/WDbhA08.png" alt="" width="48" height="42"> Spring Boot](https://spring.io/projects/spring -boot) - Spring Boot makes it easy to create standalone, production-grade Spring-based applications that you can "just run"

<!--[<img src="https://i.imgur.com/ei7nKF5.png" alt="" width="48" height="42"> Spring HATEOAS](https://spring.io /projects/spring-hateoas) - Spring HATEOAS provides some APIs to facilitate the creation of REST representations that follow the HATEOAS principle when working with Spring and especially Spring MVC -->

[<img src="https://i.imgur.com/qFZtEoa.png" alt="" width="48" height="24"> Maven](http://maven.apache.org/guides/ getting-started/) - Maven is essentially a project management and understanding tool and provides a way to help with management: Builds, Documentation, Reporting, Dependencies, SCMs, Release, Distribution and Documentation

## Troubleshooting

- Got permission denied while trying to connect to Docker daemon socket on unix:///var/run/docker.sock:
Configure routing `sudo setfacl --modify user:<user name or ID>:rw /var/run/docker.sock` ([solution](https://stackoverflow.com/a/54504083))

- If you get stuck when establishing a connection with AWS EC2, do the following:
https://stackoverflow.com/a/48572280

- Use personal computers:

 Edit the sudoers file using the `sudo visudo` command and add at the end of the file `user ALL=(ALL) NOPASSWD: ALL`,
replacing `user` with the username of the account to be used. This allows you to run sudo commands without being prompted for the password.
manager-master automates the configuration of a new edge host by creating a public-private key pair (see [ssh-keygen](https://www.ssh.com/ssh/keygen/)).

 If protected by the router (http://192.168.1.254 in the case of *meo*,
http://192.168.1.1 in the case of *nos* or *vodafone*), then you need to make settings on the router so that the machines
 are accessible outside the local network.
If the router uses [DHCP](https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol) to assign dynamic ips
To machines, you need to set a static IP for the desired host, through the router interface.
And finally, to expose the necessary ports, configure the following port forwarding in the router control panel:
 - Ssh, port 22 TCP. Access using `ssh user@ip_publico_do_router` ([see public ip](https://ipinfo.io/ip))
 - Docker Cluster management communications, TCP port 2377
 - Communication between docker/Container network discovery nodes, port 7946 TCP and UDP
 - Docker Overlay network traffic / container ingress network, UDP port 4789
 - SymmetricDS registry, port 3145 TCP and UDP
 - Docker api proxy, port 2375 TCP
 - Master manager, port 8080 TCP
 - Worker-manager, port 8081 TCP
 - Prometheus, port 9090 TCP
 - Registration-server, TCP port 8761
 - Load-balancer, port 1906 TCP

It is worth noting that, with this configuration alone, it will not be possible to access application containers outside the local network.

## License

Master manager is licensed with [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
