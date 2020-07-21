FROM openjdk:15-jdk-alpine

WORKDIR /usr/src/app

COPY ./src/main/resources/aws/ec2.pem ./ec2.pem
COPY ./src/main/resources/logging/logback-spring.xml ./src/main/resources/logging/logback-spring.xml
COPY ./target/*.jar ./app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","./app.jar"]