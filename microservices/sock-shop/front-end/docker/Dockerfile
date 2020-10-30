FROM usmanager/registration-client AS registration-client
FROM node:9-alpine

ENV NODE_ENV "production"
ENV PORT 8079
EXPOSE 8079

# Prepare app directory
WORKDIR /usr/src/app
COPY . .

RUN npm install

COPY --from=registration-client /app/registration-client registration-client
COPY scripts/docker-init.sh docker-init.sh
RUN ["chmod", "+x", "docker-init.sh"]

ENTRYPOINT ["./docker-init.sh"]

# registration-server, externalPort, internalPort, hostname
CMD ["127.0.0.1:8761", "80", "8079", "127.0.0.1"]