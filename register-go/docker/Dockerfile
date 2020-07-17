FROM golang:1.10

COPY ../. /go/src/github.com/usmanager/manager/register-go
WORKDIR /go/src/github.com/usmanager/manager/register-go

RUN go get -u github.com/FiloSottile/gvt
RUN gvt restore && \
    CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /register github.com/usmanager/manager/register-go/cmd/register-go
