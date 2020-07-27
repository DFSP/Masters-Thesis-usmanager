# Kafka and Zookeeper. Dockerfile based on spotify/kafka (hub.docker.com/r/spotify/kafka)

FROM openjdk:15-slim

ENV DEBIAN_FRONTEND noninteractive
ENV SCALA_VERSION 2.12
ENV KAFKA_VERSION 2.5.0
ENV KAFKA_HOME /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"

# Install Kafka, Zookeeper and other needed things
# jre installation requires man folder to exist
RUN mkdir -p /usr/share/man/man1 \
    && apt-get update \
    && apt-get install -y zookeeper wget supervisor dnsutils \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean \
    && wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -O /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz \
    && tar xfz /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -C /opt \
    && rm /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz

ADD start-kafka.sh /usr/bin/start-kafka.sh
RUN ["chmod", "+x", "/usr/bin/start-kafka.sh"]

# Supervisor config
ADD kafka.conf zookeeper.conf /etc/supervisor/conf.d/

# 2181 is zookeeper, 9092 is kafka
EXPOSE 2181 9092

CMD ["supervisord", "-n"]