# μsManager 

Sistema de gestão dinâmico de microserviços

μsManager é um sistema para fazer a gestão de microserviços dinamicamente, quer na cloud como na edge. 
Tenta replicar e migrar microserviços conforme a carga dos serviços, 
através da recolha de várias métricas, como utilização de cpu e ram dos dispositivos, localização dos pedidos, 
dependências entre microserviços, e dispositivos cloud e edge disponíveis.  
Este projeto está enquadrado no contexto de várias dissertações para obtenção do grau mestre em Engenharia Informática na [FCT-UNL](https://www.fct.unl.pt/).

### Organização do projeto (módulos)

- [manager-database](manager-database)

- [manager-services](manager-services)

- [manager-master](manager-master)

- [manager-worker](manager-worker)

- [manager-hub](manager-hub)

- [nginx-basic-auth-proxy](nginx-basic-auth-proxy)

- [nginx-load-balancer](nginx-load-balancer)

- [nginx-load-balancer-api](nginx-load-balancer-api)

- [prometheus](prometheus)

- [registration-server](registration-server)

- [registration-client](registration-client)

- [registration-client-java](registration-client-java)

- [registration-client-go](registration-client-go)

- [request-location-monitor](request-location-monitor)

- microserviços
  - sock-shop
    - [carts](microservices/sock-shop/carts)
    - [docker-rabbitmq](microservices/sock-shop/docker-rabbitmq)
    - [front-end](microservices/sock-shop/front-end)
    - [orders](microservices/sock-shop/orders)
    - [queue-master](microservices/sock-shop/queue-master)
    - [shipping](microservices/sock-shop/shipping)
  - mixal
    - TODO


### Ferramentas usadas

[<img src="https://i.imgur.com/c6X4nsq.png" alt="" width="48" height="48"> IntelliJ IDEA](https://www.jetbrains.com/idea/) - IntelliJ IDEA is an integrated development environment written in Java for developing computer software

As ferramentas específicas usadas em cada um dos módulos podem ser vistas nos respetivos ficheiros README.md:

> [Manager master](manager-master/README.md#ferramentas)  

> [Manager worker](manager-worker/README.md#ferramentas)  

> [Manager hub](manager-hub/README.md#ferramentas)  

> [Request location monitor](request-location-monitor/README.md#ferramentas)  

> [Registration client](registration-client/README.md#ferramentas)  

> [Registration server](registration-server/README.md#ferramentas)  

> [Nginx-load-balancer-api](registration-client/README.md#ferramentas)  

### Configuração

##### Aws

O sistema usa instâncias aws ec2 para alojar os microserviços. Para configurar, seguir:

- [Criar uma conta](https://signin.aws.amazon.com/signin?redirect_uri=https%3A%2F%2Fconsole.aws.amazon.com%2Fconsole%2Fhome%3Fstate%3DhashArgs%2523%26isauthcode%3Dtrue&client_id=arn%3Aaws%3Aiam%3A%3A015428540659%3Auser%2Fhomepage&forceMobileApp=0&code_challenge=Gzp7ZBgZKf6PFunBuy7d8chpcB2c9KDZzViYgdhBy1Q&code_challenge_method=SHA-256) no aws, caso ainda não tenha. A versão grátis deve ser suficiente

- O dashboard pode ser consultado [aqui](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#Home:).

- [Criar](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#SecurityGroups:) um Security Group, 
com nome `us-manager-security-group`, e uma Inbound rule `Custom TCP 22-80 Anywhere`

- [Iniciar uma instância](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#LaunchInstanceWizard:) 
t2-micro, com base, por exemplo, no Ubuntu Server 20.04 LTS. Guardar o ficheiro .pem na pasta /manager-master/src/main/resources/aws.
Executar `chmod 400 file.pem` no ficheiro .pem que foi transferido.

- Criar uma imagem (ami) a partir da instância iniciada anteriormente, no menu da instancia [aqui](https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#Instances:https://us-east-2.console.aws.amazon.com/ec2/v2/home?region=us-east-2#Instances:),
Image -> Create image. Após criada, adicionar a tag us-manager=true. 
Substituir o id da ami no application.yaml, propriedade aws.instance.ami.

- [Criar](https://console.aws.amazon.com/iam/home#/users) um utilizador *iam* para aceder aos recursos aws através, 
com tipo de acesso Programmatic access, e política AdministratorAccess. Substituir os valores da access key e secret access key no application.yaml, propriedades aws.access.key e aws.access.secret-key, respetivamente.

- [Configurar chaves](https://aws.amazon.com/pt/premiumsupport/knowledge-center/ec2-ssh-key-pair-regions/) em todas as regiões disponíveis.

##### Go

Para usar repositórios privados com o go modules:  
`go env -w GOPRIVATE=github.com/usmanager/*`

### Licença

μsManager está licenciado com a [MIT license](LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.

