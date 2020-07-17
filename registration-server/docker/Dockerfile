FROM danielfct/jdk-glibc
WORKDIR /usr/src/app
COPY ../target/*.jar ./app.jar
COPY ../scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# externalPort, internalPort, hostname, zone
CMD ["8761", "8761", "127.0.0.1", "http://127.0.0.1:8761/eureka/"]