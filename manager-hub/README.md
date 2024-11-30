# Manager hub

[![js-eslint-style](https://img.shields.io/badge/code%20style-TSLint-blue.svg?style=flat-square)](https://palantir.github.io/ tslint/)

This module consists of a web client developed with [reactjs](https://docs.npmjs.com/)
and [typescript](https://www.typescriptlang.org/docs/home.html). It uses [react-redux](https://redux.js.org/) to manage the data.

Communicates with [Manager Master](../manager-master) via [REST API](https://restfulapi.net/) to allow
manually adjust and interact with the system, as well as allowing visualization of the progress and behavior of the system as a whole.

<sup>Note: See the beginning of [DatabaseLoader](../manager-master/src/main/java/pt/unl/fct/miei/usmanagement/manager/database/DatabaseLoader.java) of the master manager to get the credentials.</sup>

### Install

```shell script
npm install
```

### Start

```shell script
npm start
```

###Docker

##### Development environment

```shell script
docker build -f docker/Dockerfile.dev . -t dev-manager-hub
docker run -it --rm -v ${PWD}:/app -v /app/node_modules -p 3001:3000 -e CHOKIDAR_USEPOLLING=true dev-manager-hub
```

##### Production environment
```shell script
docker build -f docker/Dockerfile . -t hub-manager
docker run -it --rm -p 80:80 manager-hub
```

##### Docker hub
```shell script
docker run -it --rm -p 80:80 usmanager/manager-hub
```

### Tests

 Tested in the environment:

> Ubuntu 20.04.1 LTS
> Chrome Browser 86.0.4240.111

### Tunnelto

Allows you to expose the server to the public through a link:
Download [here](https://github.com/agrinman/tunnelto/releases/download/0.1.9/tunnelto-linux.tar.gz).
To execute:
```shell script
tunnelto --port 3000
```

### Tools

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40">React Developer Tools](https://chrome.google.com/ webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi?hl=en) - Adds React debugging tools to Chrome developer tools

### Useful guides

[<img src="https://i.imgur.com/GBqHVDe.png" alt="" width="48" height="15"> npm](https://docs.npmjs.com/) - npm is the largest software registry in the world

[<img src="https://i.imgur.com/LGowRP4.png" alt="" width="48" height="40"> Reactjs](https://reactjs.org/docs/getting- started.html) - React is a JavaScript library for building user interface

[<img src="https://i.imgur.com/lwAbTpS.png" alt="" width="48" height="48"> Typescript](https://www.typescriptlang.org/docs/ home.html) - TypeScript is a typed superset of JavaScript that compiles to plain JavaScript

[<img src="https://i.imgur.com/7C87tJD.png" alt="" width="48" height="48"> React-redux](https://redux.js.org/ ) - A Predictable State Container for JS Apps

### Error resolution

If after running `npm start`, the error appears:

> ENOSPC: System limit for number of file watchers reached

Running the following commands should resolve it:

```shell script
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf`
sudo sysctl -p
```

### License

Manager hub is licensed with [MIT license](../LICENSE). See the license in the header of the respective file to confirm.
