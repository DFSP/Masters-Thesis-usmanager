FROM maven:3.6.0-jdk-11-slim

WORKDIR /usr/src/app

# install
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml install

# cleanup
RUN rm -rf src pom.xml target