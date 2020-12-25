FROM golang:1.14.6-alpine AS build

# Build request-location-monitor binary
COPY . /go/src/github.com/usmanager/manager/request-location-monitor
WORKDIR /go/src/github.com/usmanager/manager/request-location-monitor
RUN go build -o request-location-monitor && \
mkdir /app && \
mv request-location-monitor /app/request-location-monitor && \
mv docker/docker-init.sh /docker-init.sh

# Build final image
FROM alpine
RUN mkdir /app
WORKDIR /app
COPY --from=build /app/request-location-monitor request-location-monitor
COPY --from=build docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]
ENTRYPOINT ["./docker-init.sh"]
CMD ["1919", "60000"]