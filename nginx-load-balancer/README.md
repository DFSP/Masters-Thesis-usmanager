# Nginx-load-balancer

Este módulo contém os ficheiros necessários para iniciar o load-balancer [Nginx](http://nginx.org/en/docs/http/load_balancing.html).

### Docker
 
```
docker build -f docker/Dockerfile . -t nginx-load-balancer  
docker run --rm -p 1906:80 nginx-load-balancer
```