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
COPY manager-worker/src src
COPY manager-worker/pom.xml .
RUN mvn -DskipTests clean package

FROM adoptopenjdk/openjdk11:ubuntu-jre
COPY manager-worker/src/main/docker/scripts scripts
RUN sh scripts/dockerfile-docker-install.sh scripts/dockerfile-docker-api-install.sh
WORKDIR /app
COPY --from=build /usr/src/app/target/*.jar manager-worker.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","manager-worker.jar"]