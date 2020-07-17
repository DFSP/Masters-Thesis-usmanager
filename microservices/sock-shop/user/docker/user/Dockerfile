FROM danielfct/register-go

FROM golang:1.10-alpine
COPY . /go/src/bitbucket.org/microservicemanagement/user
WORKDIR /go/src/bitbucket.org/microservicemanagement/user
RUN apk update
RUN apk add git
RUN go get -v github.com/Masterminds/glide 
RUN glide install && CGO_ENABLED=0 go build -a -installsuffix cgo -o /app main.go

FROM danielfct/alpine-glibc

ENV HATEAOS user
ENV USER_DATABASE mongodb
ENV MONGO_HOST user-db:27017

WORKDIR /
COPY --from=0 /register ./register
COPY --from=1 /app /app
COPY ./scripts/docker-init.sh ./docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' /app

ENTRYPOINT ["./docker-init.sh"]

# eureka, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "user-db:27017"]
