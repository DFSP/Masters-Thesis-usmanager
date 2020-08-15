# Manager web

[![js-eslint-style](https://img.shields.io/badge/code%20style-TSLint-blue.svg?style=flat-square)](https://palantir.github.io/tslint/)

Este módulo é consistido por um cliente web reactjs.  
O qual comunica com o [Manager Master](/usmanager/manager-master) através de REST API para permitir 
ajustar manualmente e interagir com o sistema, bem como a visualização do progresso e comportamento do sistema como um todo.  
 
 ### Instalar
 
```shell script
npm install
```
 
 ### Iniciar
 
```shell script
npm start
```
 
### Docker
 
```shell script
docker build -f docker/Dockerfile . -t manager-web
docker run -it --rm -v ${PWD}:/app -v /app/node_modules -p 3001:3000 -e CHOKIDAR_USEPOLLING=true manager-web
```

### Ambiente
 
> Ubuntu 18.04.4 LTS  
 
> Chrome browser 84.0.4147.89

### Ferramentas usadas

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40">React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi?hl=en) - Adds React debugging tools to the Chrome Developer Tools

### Guias úteis

[<img src="https://i.imgur.com/GBqHVDe.png" alt="" width="48" height="15"> npm](https://docs.npmjs.com/) - npm is the world’s largest software registry

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40"> Reactjs](https://reactjs.org/docs/getting-started.html) - React is a JavaScript library for building user interfaces

[<img src="https://i.imgur.com/lwAbTpS.png" alt="" width="48" height="48"> Typescript](https://www.typescriptlang.org/docs/home.html) - TypeScript is a typed superset of JavaScript that compiles to plain JavaScript

### Resolução de erros

Se após a execução de `npm start`, por acaso aparecer o erro:

> ENOSPC: System limit for number of file watchers reached

Executar os comandos seguintes deve resolvê-lo:

```shell script
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf`
sudo sysctl -p
```

### Lista TO-DO

- O código pode ser fatorizado um pouco mais. 
Por exemplo, existir um componente genérico Entity que representa as entidades principais,
como app, service, container, host, etc. E um EntityList para representar as listas das entidades, 
por exemplo dos serviços da apps, ou das regras de um container.
Iria permitir a remoção de algum código que parece repetido.

- Outro melhoramento seria o uso de listas animadas, trabalho o qual já foi iniciado no componente AnimatedList (ver [react-spring](https://www.react-spring.io/)).
Por exemplo, ao remover/adicionar novos elementos a uma lista, ou ao mudar de página numa lista paginada.

### Licença

Web manager está licenciado com a [MIT license](../LICENSE). Ver a licença no cabeçalho do respetivo ficheiro para confirmar.
