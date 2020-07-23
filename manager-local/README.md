# Manager local

Gestor local que faz a conexão entre as máquinas edge e cloud.
Gere um conjunto de nós e containers na edge.  
Usa [kafka](https://kafka.apache.org/) para comunicar com o manager-master.  
É um módulo java gerido com maven.

## Dependências

- [JCommander](http://jcommander.org/) - Commander is a very small Java framework that makes it trivial to parse command line parameters
- [kafka](https://kafka.apache.org/) - Kafka® is used for building real-time data pipelines and streaming apps