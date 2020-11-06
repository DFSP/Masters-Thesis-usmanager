FROM maven:3.6.0-jdk-11-slim as build

WORKDIR /usr/src/app

# install
COPY src ./src
COPY pom.xml .
RUN mvn clean install -DskipTests -Dmaven.javadoc.skip=true

FROM alpine
COPY --from=build /root/.m2 /root/.m2