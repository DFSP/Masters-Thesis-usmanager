FROM golang:1.10

COPY ../. /go/src/bitbucket.org/microservicemanagement/nginx-load-balancer-api
WORKDIR /go/src/bitbucket.org/microservicemanagement/nginx-load-balancer-api

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /nginx-load-balancer-api bitbucket.org/microservicemanagement/nginx-load-balancer-api/cmd