# Nginx load balancer API
API to add new servers to Nginx load balancer and generate new config files


### To build this service

#### Dependencies
```
go get -u github.com/FiloSottile/gvt
gvt restore
```

#### Go tools
In order to build the project locally you need to make sure that the repository directory is located in the correct
$GOPATH directory: $GOPATH/src/bitbucket.org/cloudedgemicroserviceteam/nginx-load-balancer-api/. Once that is in place you can build by running:

```
cd $GOPATH/src/bitbucket.org/cloudedgemicroserviceteam/nginx-load-balancer-api/cmd/
go build -o nginx-load-balancer-api
```

The result is a binary named `nginx-load-balancer-api`, in the current directory.

#### API
##### Default port: 1906
```
# List current servers
/_/nginx-load-balancer-api/api/servers : GET
```

```
# Add new servers
# request example : [{"hostname" : "server1:8080"}]
/_/nginx-load-balancer-api/api/servers : POST
```

```
# Deletes a server
# request example : {"hostname" : "server1:8080"}
/_/nginx-load-balancer-api/api/servers : DELETE
```



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