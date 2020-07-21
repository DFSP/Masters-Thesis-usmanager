# Prometheus

Este módulo contém os ficheiros necessários para iniciar o componente [Prometheus](https://prometheus.io/), 
configurado com o [node_exporter](https://prometheus.io/docs/guides/node-exporter/).

### Docker
 
```shell script
docker build -f docker/Dockerfile . -t prometheus  
docker run --rm -p 9090:9090 prometheus
```
