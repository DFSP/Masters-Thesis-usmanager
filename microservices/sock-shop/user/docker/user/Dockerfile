FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build user binary
COPY . /go/src/github.com/usmanager/manager/microservices/sock-shop/user
WORKDIR /go/src/github.com/usmanager/manager/microservices/sock-shop/user
RUN go build -o user && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/microservices/sock-shop/user/user /app/user

# Build final image
FROM usmanager/registration-client AS registration-client
FROM alpine:3.12.0
ENV HATEAOS user
ENV USER_DATABASE mongodb
ENV MONGO_HOST user-db:27017
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/user user
COPY --from=registration-client /app/registration-client registration-client
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["./docker-init.sh"]
# registration-server, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "user-db:27017"]