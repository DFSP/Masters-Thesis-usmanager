#Load-tests

Testes de carga efetuados aos componentes do sistema.

## Serviços

### Utilização

SERVICE_ADDRESS é o endereço do serviço ao qual serão feitos os testes de carga, que pode estar situado na periferia da rede ou estar numa máquina virtual na cloud.

##### Localmente
```shell script
k6 run -e SERVICE_ADDRESS=hostname:porta sockShopCatalogue.js
```

##### Localmente mas com resultados enviados para a cloud
```shell script
k6 login cloud -t [chave]
k6 run -o cloud -e SERVICE_ADDRESS=hostname:porta sockShopCatalogue.js
```

##### Na cloud
```shell script
k6 login cloud -t [chave]
k6 cloud -e SERVICE_ADDRESS=hostname:porta sockShopCatalogue.js
```

## Tempos de execução

METHOD = HEAD | DELETE | POST | GET | OPTIONS | PATCH | PUT  
URL é o url relativo ao caminho a ser executado:
http://localhost:8080/api/{URL}  
HOST_ADDRESS é opcional, indica o endereço do host do gestor. Caso não presente, é usado o localhost.
ITERATIONS é opcional, indica o número de execuções a fazer. Caso não presente, é feita 1 iteração.

Exemplos:

Iniciar um contentor do serviço sock-shop-catalogue.
```shell script
k6 run \
  -e METHOD=POST \
  -e URL=containers \
  -e REQUEST_BODY='{"service":"sock-shop-catalogue","externalPort":8083,"internalPort":80,"hostAddress":{"username":"daniel","publicIpAddress":"192.168.1.93","privateIpAddress":"192.168.1.93","coordinates":{"label":"Portugal","latitude":39.575097,"longitude":-8.909794}}}' \
  executionTimes.js
```

Iniciar um servidor de registo.
```shell script
k6 run \
  -e METHOD=POST \
  -e URL=registration-servers \
  -e REQUEST_BODY='{"regions":["Europe"]}' \
  executionTimes.js
```
Iniciar um balanceador de carga.
```shell script
k6 run \
  -e METHOD=POST \
  -e URL=load-balancers \
  -e REQUEST_BODY='{"regions":["Europe"]}' \
  executionTimes.js
```

Iniciar um gestor local.
```shell script
k6 run \
  -e METHOD=POST \
  -e URL=worker-managers \
  -e REQUEST_BODY='{"regions":["Europe"]}' \
  executionTimes.js
```

Iniciar um agente kafka.
```shell script
k6 run \
  -e METHOD=POST \
  -e URL=kafka \
  -e REQUEST_BODY='{"regions":["Europe"]}' \
  executionTimes.js
```
