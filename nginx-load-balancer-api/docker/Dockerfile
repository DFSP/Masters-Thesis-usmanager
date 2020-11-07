FROM golang:1.14.6-alpine AS build

# Build nginx-load-balancer-api binary
COPY . /go/src/github.com/usmanager/manager/nginx-load-balancer-api
WORKDIR /go/src/github.com/usmanager/manager/nginx-load-balancer-api
RUN go build -o nginx-load-balancer-api && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/nginx-load-balancer-api/nginx-load-balancer-api /app/nginx-load-balancer-api

# Build final image
FROM alpine
COPY --from=build /app/nginx-load-balancer-api /nginx-load-balancer-api