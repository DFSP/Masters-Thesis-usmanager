FROM golang:1.14.6-alpine

COPY . /go/src/github.com/usmanager/manager/registration-client
WORKDIR /go/src/github.com/usmanager/manager/registration-client/cmd

RUN go mod vendor
RUN go build -o registration-client