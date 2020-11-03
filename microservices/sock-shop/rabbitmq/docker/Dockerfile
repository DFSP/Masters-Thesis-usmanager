FROM usmanager/registration-client-java as registration-client-java
FROM usmanager/registration-client AS registration-client

FROM rabbitmq:alpine

# include dependencies
COPY --from=registration-client-java /root/.m2 /root/.m2

COPY --from=registration-client /app/registration-client ./registration-client
COPY scripts/docker-init.sh ./docker-init.sh

RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# rabbitmq ports: 4369 5671 5672 25672
# registration-server, externalPort, internalPort, hostname
CMD ["127.0.0.1:8761", "5672", "5672", "127.0.0.1"]