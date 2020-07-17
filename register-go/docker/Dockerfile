FROM golang:1.10

COPY ../. /go/src/bitbucket.org/microservicemanagement/register-go
WORKDIR /go/src/bitbucket.org/microservicemanagement/register-go

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /register bitbucket.org/microservicemanagement/register-go/cmd/register-go
