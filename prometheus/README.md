# Prometheus with node exporter

This module contains the files needed to start the [Prometheus](https://prometheus.io/) component,
configured with [node_exporter](https://prometheus.io/docs/guides/node-exporter/).

### Node_exporter

The node exporter is a Prometheus module that allows you to obtain metrics related to the host where you run.

##### Install

```shell script
sh node-exporter-install.sh
```

##### Start

```shell script
node_exporter
```

### Prometheus

##### Docker

```shell script
docker build -f docker/Dockerfile . -t prometheus
docker run --rm -p 9090:9090 prometheus
```

### Accessing metrics

http://localhost:9090/graph

You can try, using the queries that are in the enum [PrometheusQuery](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/monitoring/PrometheusQuery.java).

## License

Prometheus is licensed under [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
