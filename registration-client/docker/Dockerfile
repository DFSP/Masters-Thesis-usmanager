FROM golang:1.14.6-alpine

COPY . /go/src/github.com/usmanager/manager/registration-client
WORKDIR /go/src/github.com/usmanager/manager/registration-client/cmd

RUN go mod vendor && \
go build -o registration-client && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/registration-client/cmd/registration-client /app/registration-client

WORKDIR /app
RUN ["chmod", "+x", "registration-client"]