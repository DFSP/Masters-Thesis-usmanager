FROM usmanager/manager-database as manager-database

FROM maven:3.6.0-jdk-11-slim

# include dependencies
COPY --from=manager-database /root/.m2 /root/.m2

WORKDIR /usr/src/app

# install
COPY src ./src
COPY pom.xml .
RUN mvn clean install -DskipTests

# cleanup
RUN rm -rf src pom.xml target