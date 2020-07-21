FROM usmanager/registration-client AS registration-client

FROM golang:1.14.6-alpine AS build
COPY . go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue
WORKDIR go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue/cmd/cataloguesvc
RUN go mod vendor
RUN go build -o catalogue
RUN mkdir app
RUN mv go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue/cmd/cataloguesvc/catalogue app/catalogue

FROM usmanager/alpine-glibc AS catalogue

RUN mkdir app
WORKDIR app
COPY --from=build /app/catalogue catalogue
COPY --from=registration-client /app/registration-client registration-client

COPY images images
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' app

ENTRYPOINT ["./docker-init.sh"]

# eureka, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "catalogue-db:3306"]