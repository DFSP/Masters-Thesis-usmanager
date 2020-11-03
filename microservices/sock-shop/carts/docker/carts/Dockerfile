FROM usmanager/registration-client-java as registration-client-java
FROM usmanager/registration-client AS registration-client

FROM maven:3.6.0-jdk-11-slim AS build

# include dependencies
COPY --from=registration-client-java /root/.m2 /root/.m2

WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml clean package

FROM openjdk:8-jdk-alpine
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/target/carts.jar carts.jar

COPY --from=registration-client /app/registration-client registration-client

COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# registration-server, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "carts-db:27017"]