FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build registration-client binary
COPY . /go/src/github.com/usmanager/manager/registration-client
WORKDIR /go/src/github.com/usmanager/manager/registration-client
RUN go build -o registration-client && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/registration-client/registration-client /app/registration-client

# Build final image
FROM alpine
COPY --from=build /app/registration-client registration-client
CMD ./registration-client