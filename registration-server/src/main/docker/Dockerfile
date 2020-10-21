FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /usr/src/app

# install
COPY src ./src
COPY pom.xml .
RUN mvn clean package -DskipTests
RUN mv target/*.jar ./registration-server.jar

# cleanup
RUN rm -rf /root/.m2 src pom.xml target

# execution
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
EXPOSE 8761
ENTRYPOINT ["./docker-init.sh"]
# externalPort, internalPort, hostname
CMD ["8761", "8761", "127.0.0.1"]