FROM danielfct/registration-client

FROM golang:1.10
COPY . /go/src/bitbucket.org/microservicemanagement/payment
WORKDIR /go/src/bitbucket.org/microservicemanagement/payment

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app bitbucket.org/microservicemanagement/payment/cmd/paymentsvc

FROM danielfct/alpine-glibc
WORKDIR /
COPY --from=0 /register ./register
COPY --from=1 /app /app
COPY ./scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' /app

ENTRYPOINT ["./docker-init.sh"]

# eureka, externalPort, internalPort, hostname
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1"]