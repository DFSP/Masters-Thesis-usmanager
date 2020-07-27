# Manager local

Gestor local que faz a conexão entre as máquinas edge e cloud.
Gere um conjunto de nós e containers na edge.  
Usa [kafka](https://kafka.apache.org/) para comunicar com o manager-master.  
É um módulo java gerido com maven.

## Dependências

- [Spring boot]()
- [Mongodb](https://www.mongodb.com/) - MongoDB is a document database, which means it stores data in JSON-like documents
- [kafka](https://kafka.apache.org/) - Kafka® is used for building real-time data pipelines and streaming apps

## Instalar

Mongodb
```shell script
sudo apt install -y mongodb
```