FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build nginx-load-balancer-api binary
COPY . /go/src/github.com/usmanager/manager/nginx-load-balancer-api
WORKDIR /go/src/github.com/usmanager/manager/nginx-load-balancer-api
RUN go mod vendor && \
go build -o nginx-load-balancer-api && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/nginx-load-balancer-api/nginx-load-balancer-api /app/nginx-load-balancer-api

# Build final image
FROM alpine
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/nginx-load-balancer-api nginx-load-balancer-api