# Registration-client
Register a service on a Eureka server and gets other apps endpoints from Eureka.


### To build this service

#### Dependencies
```
go get -u github.com/FiloSottile/gvt
gvt restore
```

#### Go tools
In order to build the project locally you need to make sure that the repository directory is located in the correct
$GOPATH directory: $GOPATH/src/github.com/usmanager/manager/registration-client/. Once that is in place you can build by running:

```
cd $GOPATH/src/github.com/usmanager/manager/registration-client/cmd/registration-client/
go build -o registration-client
```

The result is a binary named `registration-client`, in the current directory.

### API Endpoints

All URIs are relative to *http://localhost:1906/api*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AppsApi* | **GetAllAppsByName** | **Get** /apps/{appName}/all | Get all apps endpoints by app name
*AppsApi* | **GetAppsByName** | **Get** /apps/{appName} | Get an app endpoint by app name



## License

MIT License

Copyright (c) 2018 André Carrusca

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.