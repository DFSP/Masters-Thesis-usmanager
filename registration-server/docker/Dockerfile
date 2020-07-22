FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml clean package

FROM usmanager/jdk-glibc
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/target/registration-server.jar registration-server.jar

COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# externalPort, internalPort, hostname
CMD ["8761", "8761", "127.0.0.1"]