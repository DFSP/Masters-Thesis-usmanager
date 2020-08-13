FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml install