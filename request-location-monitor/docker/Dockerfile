FROM golang:1.14.6-alpine AS build

# Setup go to be able to get usmanager private repository
ENV GITHUB_TOKEN 52c6058781e851556a74229c42e819c661a7a49a
RUN apk add git && \
git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/usmanager".insteadOf "https://github.com/usmanager" && \
go env -w GOPRIVATE=github.com/usmanager/*

# Build request-location-monitor binary
COPY . /go/src/github.com/usmanager/manager/request-location-monitor
WORKDIR /go/src/github.com/usmanager/manager/request-location-monitor
RUN go mod vendor && \
go build -o request-location-monitor && \
mkdir /app && \
mv /go/src/github.com/usmanager/manager/request-location-monitor/request-location-monitor /app/request-location-monitor

# Build final image
FROM alpine
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/request-location-monitor request-location-monitor
ENV port 1919
ENV interval 60000
CMD ./request-location-monitor -port $port -interval $interval