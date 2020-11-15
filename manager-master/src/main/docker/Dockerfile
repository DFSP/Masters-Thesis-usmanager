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

FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk add bash
WORKDIR /app
COPY --from=build /usr/src/app/target/*.jar manager-master.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","manager-master.jar"]