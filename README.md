# μsManager 

Sistema de gestão dinâmico de microserviços

μsManager é um sistema para fazer a gestão de microserviços dinamicamente, quer na cloud como na edge. 
Tenta replicar e migrar microserviços conforme a carga dos serviços, 
através da recolha de várias métricas, como utilização de cpu e ram dos dispositivos, localização dos pedidos, 
dependências entre microserviços, e dispositivos cloud e edge disponíveis.  
Este projeto está enquadrado no contexto de várias dissertações para obtenção do grau mestre em Engenharia Informática na [FCT-UNL](https://www.fct.unl.pt/).

### Organização do projeto

Tem 3 vertentes principais:
 - **Gestão** - relacionado com a gestão das réplicas dos microserviços e gestão das máquinas/nós incluidas no sistema
 - **Monitorização** - trata da recolha de métricas de aplicação, nós e *docker containers*
 - **Dados** - gere a replicação e migração dos dados, garantindo a consistência dos dados das aplicações

### Licença

μsManager está licenciado com o [MIT license](https://github.com/usmanager/usmanager/LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.
