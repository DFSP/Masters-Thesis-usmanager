FROM mysql:5.7

ENV MYSQL_ROOT_PASSWORD DEFAULT_PASS
ENV MYSQL_ALLOW_EMPTY_PASSWORD=true
ENV MYSQL_DATABASE=socksdb

COPY ./docker/catalogue-db/data/dump.sql /docker-entrypoint-initdb.d/