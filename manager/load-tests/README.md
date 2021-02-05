#Load-tests

Testes de carga efetuados aos componentes do sistema.

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


