FROM golang:1.10

COPY ../. /go/src/bitbucket.org/microservicemanagement/request-location-monitor
WORKDIR /go/src/bitbucket.org/microservicemanagement/request-location-monitor

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app bitbucket.org/microservicemanagement/request-location-monitor/cmd

FROM danielfct/alpine-glibc
WORKDIR /
COPY --from=0 /app /app
COPY ./scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' /app

ENTRYPOINT ["./docker-init.sh"]

# internalPort, interval
CMD ["1919", "60"]