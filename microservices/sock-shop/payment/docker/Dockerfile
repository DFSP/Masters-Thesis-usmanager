FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build payment binary
COPY . /go/src/github.com/usmanager/manager/microservices/sock-shop/payment
WORKDIR /go/src/github.com/usmanager/manager/microservices/sock-shop/payment/cmd/paymentsvc
RUN go build -o payment && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/microservices/sock-shop/payment/cmd/paymentsvc/payment /app/payment

# Build final image
FROM usmanager/registration-client AS registration-client
FROM alpine:3.12.0
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/payment payment
COPY --from=registration-client /app/registration-client registration-client
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["./docker-init.sh"]
# registration-server, externalPort, internalPort, hostname
CMD ["127.0.0.1:8761", "8080", "80", "127.0.0.1"]