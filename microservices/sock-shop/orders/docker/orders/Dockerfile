FROM usmanager/registration-client

FROM usmanager/jdk-glibc
WORKDIR /usr/src/app
COPY --from=0 /registration-client ./registration-client
COPY ./target/*.jar ./app.jar
COPY ./scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# eureka, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "orders-db:27017"]