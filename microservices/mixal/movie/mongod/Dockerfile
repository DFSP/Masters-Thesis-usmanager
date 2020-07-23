FROM mongo:latest
RUN mkdir /db
COPY ./db /db
RUN chown -R mongodb:mongodb /db && chmod -R 777 /db
EXPOSE 27017
CMD ["mongod", "--dbpath=/db"]