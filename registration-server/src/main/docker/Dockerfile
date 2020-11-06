FROM maven:3.6.1-jdk-8-alpine AS build

WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests clean package

FROM openjdk:8-jre-alpine

RUN apk add bash

WORKDIR /app
COPY --from=build /usr/src/app/target/*.jar registration-server.jar
COPY scripts/docker-init.sh .
RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["/bin/bash", "-c", "./docker-init.sh"]
ENV internalPort 8761
ENV externalPort 8761
ENV hostname 127.0.0.1
CMD ${internalPort} ${externalPort} ${hostname}