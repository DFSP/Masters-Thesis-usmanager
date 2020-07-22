FROM usmanager/registration-client as registration-client
FROM rabbitmq:alpine

COPY --from=registration-client /app/registration-client ./registration-client
COPY docker-init.sh ./docker-init.sh

RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# rabbitmq ports: 4369 5671 5672 25672
# eureka, externalPort, internalPort, hostname
CMD ["127.0.0.1:8761", "5672", "5672", "127.0.0.1"]