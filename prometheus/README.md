# Prometheus com Node exporter

Este módulo contém os ficheiros necessários para iniciar o componente [Prometheus](https://prometheus.io/), 
configurado com o [node_exporter](https://prometheus.io/docs/guides/node-exporter/).

### Node_exporter

O node exporter é um módulo do prometheus que permite obter métricas relativas ao host onde executa.

##### Instalar

```shell script
sh node-exporter-install.sh
```

##### Iniciar

```shell script
node_exporter
```

### Prometheus
 
##### Docker

```shell script
docker build -f docker/Dockerfile . -t prometheus  
docker run --rm -p 9090:9090 prometheus
```

### Aceder às métricas

http://localhost:9090/graph

Pode experimentar, usando as queries que estão no enum [PrometheusQuery](../manager-database/src/main/java/pt/unl/fct/miei/usmanagement/manager/monitoring/PrometheusQuery.java).

## Licença

Prometheus está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.
 