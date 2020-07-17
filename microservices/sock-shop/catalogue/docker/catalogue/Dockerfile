FROM danielfct/register-go

FROM golang:1.10
COPY . /go/src/bitbucket.org/microservicemanagement/catalogue
WORKDIR /go/src/bitbucket.org/microservicemanagement/catalogue

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app bitbucket.org/microservicemanagement/catalogue/cmd/cataloguesvc

FROM danielfct/alpine-glibc
WORKDIR /
COPY --from=0 /register ./register
COPY --from=1 /app /app
COPY images/ /images/
COPY ./scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' /app

ENTRYPOINT ["./docker-init.sh"]

# eureka, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "catalogue-db:3306"]