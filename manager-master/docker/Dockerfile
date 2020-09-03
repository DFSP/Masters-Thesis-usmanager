FROM usmanager/manager-database as database

FROM maven:3.6.0-jdk-11-slim
COPY --from=database /root/.m2 /root/.m2

WORKDIR /usr/src/app

COPY src ./src
COPY pom.xml .
RUN mvn -DskipTests -f pom.xml clean package
RUN mv target/*.jar ./manager-master.jar

RUN rm -rf /root/.m2 src pom.xml target

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./manager-master.jar"]