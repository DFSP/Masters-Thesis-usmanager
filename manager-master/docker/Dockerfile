FROM usmanager/manager-database as manager-database
FROM usmanager/manager-services as manager-services

FROM maven:3.6.0-jdk-11-slim

# include dependencies
COPY --from=manager-database /root/.m2 /root/.m2
COPY --from=manager-services /root/.m2 /root/.m2

WORKDIR /usr/src/app

# install
COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml clean package
RUN mv target/*.jar ./manager-master.jar

# cleanup
RUN rm -rf /root/.m2 src pom.xml target

# execute
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./manager-master.jar"]