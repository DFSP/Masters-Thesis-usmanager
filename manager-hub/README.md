# Manager hub

[![js-eslint-style](https://img.shields.io/badge/code%20style-TSLint-blue.svg?style=flat-square)](https://palantir.github.io/tslint/)

Este módulo é consistido por um cliente web desenvolvido com [reactjs](https://docs.npmjs.com/) 
e [typescript](https://www.typescriptlang.org/docs/home.html). Usa [react-redux](https://redux.js.org/) para gerir os dados.

Comunica com o [Manager Master](../manager-master) através de [REST API](https://restfulapi.net/) para permitir 
ajustar manualmente e interagir com o sistema, bem como permitir a visualização do progresso e comportamento do sistema como um todo.  
 
<sup>Nota: Ver o início do [DatabaseLoader](../manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/database/DatabaseLoader.java) do manager master para obter as credenciais.</sup>
 
### Instalar
 
```shell script
npm install
```
 
### Iniciar
 
```shell script
npm start
```
 
### Docker
 
##### Ambiente de desenvolvimento
 
```shell script
docker build -f docker/Dockerfile.dev . -t dev-manager-hub
docker run -it --rm -v ${PWD}:/app -v /app/node_modules -p 3001:3000 -e CHOKIDAR_USEPOLLING=true dev-manager-hub
```

##### Ambiente de produção
```shell script
docker build -f docker/Dockerfile . -t manager-hub
docker run -it --rm -p 80:80 manager-hub
```

##### Docker hub
```shell script
docker run -it --rm -p 80:80 usmanager/manager-hub
```

### Testes
 
 Testado no ambiente:
 
> Ubuntu 20.04.1 LTS
> Chrome browser 86.0.4240.111 

### Ferramentas

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40">React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi?hl=en) - Adds React debugging tools to the Chrome Developer Tools

### Guias úteis

[<img src="https://i.imgur.com/GBqHVDe.png" alt="" width="48" height="15"> npm](https://docs.npmjs.com/) - npm is the world’s largest software registry

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40"> Reactjs](https://reactjs.org/docs/getting-started.html) - React is a JavaScript library for building user interfaces

[<img src="https://i.imgur.com/lwAbTpS.png" alt="" width="48" height="48"> Typescript](https://www.typescriptlang.org/docs/home.html) - TypeScript is a typed superset of JavaScript that compiles to plain JavaScript

[<img src="https://i.imgur.com/7C87tJD.png" alt="" width="48" height="48"> React-redux](https://redux.js.org/) - A Predictable State Container for JS Apps

### Resolução de erros

Se após a execução de `npm start`, por acaso aparecer o erro:

> ENOSPC: System limit for number of file watchers reached

Executar os comandos seguintes deve resolvê-lo:

```shell script
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf`
sudo sysctl -p
```

### Licença

Manager hub está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.
