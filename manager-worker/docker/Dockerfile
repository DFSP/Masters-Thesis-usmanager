FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -f pom.xml clean package

FROM openjdk:11-jre-slim
COPY --from=build /usr/src/app/target/*.jar ./manager-worker.jar
EXPOSE 8181

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./manager-worker.jar"]