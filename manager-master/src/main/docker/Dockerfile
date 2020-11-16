FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app/manager-database
COPY manager-database/src ./src
COPY manager-database/pom.xml ./pom.xml
RUN mvn install -DskipTests -U

WORKDIR /usr/src/app/manager-services
COPY manager-services/src ./src
COPY manager-services/pom.xml ./pom.xml
RUN mvn install -DskipTests -U

WORKDIR /usr/src/app
COPY manager-master/src src
COPY manager-master/pom.xml .
RUN mvn -DskipTests clean package
RUN sh src/main/resources/script/docker-install.sh && sh src/main/resources/script/docker-api-install.sh

FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk add bash util-linux && \
sh src/main/docker/scripts/docker-install-dockerfile.sh src/main/docker/scripts/docker-api-install-dockerfile.sh && \
docker ls
WORKDIR /app
COPY --from=build /usr/src/app/target/*.jar manager-master.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","manager-master.jar"]