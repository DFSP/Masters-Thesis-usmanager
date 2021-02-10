FROM golang:1.14.6-alpine AS build

# Build registration-client binary
COPY . /go/src/github.com/usmanager/manager/registration-client
WORKDIR /go/src/github.com/usmanager/manager/registration-client
RUN go build -o registration-client && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/registration-client/registration-client /app/registration-client

# Build final image
FROM alpine

COPY --from=build /app/registration-client /app/registration-client

CMD ./app/registration-client