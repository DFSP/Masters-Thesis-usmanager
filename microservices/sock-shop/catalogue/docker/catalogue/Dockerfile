FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build catalogue binary
COPY . /go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue
WORKDIR /go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue/cmd/cataloguesvc
RUN go build -o catalogue && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/microservices/sock-shop/catalogue/cmd/cataloguesvc/catalogue /app/catalogue

# Build final image
FROM usmanager/registration-client AS registration-client
FROM alpine:3.12.0
RUN apk add libcap
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/catalogue catalogue
COPY --from=registration-client /app/registration-client registration-client
COPY images images
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
RUN setcap 'cap_net_bind_service=+ep' /app/catalogue
ENTRYPOINT ["./docker-init.sh"]
# registration-server, externalPort, internalPort, hostname, db
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1", "catalogue-db:3306"]