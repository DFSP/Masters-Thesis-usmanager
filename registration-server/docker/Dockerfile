FROM usmanager/jdk-glibc
WORKDIR /usr/src/app
COPY target/*.jar ./app.jar
COPY scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# externalPort, internalPort, hostname
CMD ["8761", "8761", "127.0.0.1"]