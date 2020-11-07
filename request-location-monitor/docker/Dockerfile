FROM golang:1.14.6-alpine AS build

# Build request-location-monitor binary
COPY . /go/src/github.com/usmanager/manager/request-location-monitor
WORKDIR /go/src/github.com/usmanager/manager/request-location-monitor
RUN go build -o request-location-monitor && \
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