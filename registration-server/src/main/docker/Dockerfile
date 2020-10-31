FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /usr/src/app

# install
COPY scripts/docker-init.sh .
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests clean package
RUN mv target/*.jar ./registration-server.jar

# cleanup
RUN rm -rf /root/.m2 src pom.xml target

# execution
RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["/bin/bash", "-c", "./docker-init.sh"]
ENV internalPort 8761
ENV externalPort 8761
ENV hostname 127.0.0.1
CMD ${internalPort} ${externalPort} ${hostname}