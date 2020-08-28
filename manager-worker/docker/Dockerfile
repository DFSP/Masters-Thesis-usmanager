FROM usmanager/manager-database as database

FROM maven:3.6.0-jdk-11-slim AS build
COPY --from=database /root/.m2 /root/.m2
WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml clean package

FROM openjdk:11-jre-slim
COPY --from=build /usr/src/app/target/*.jar ./manager-worker.jar
EXPOSE 8081
ENV master 127.0.0.1

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./manager-worker.jar","--external-id=${id}", "--master-hostname=${master}"]