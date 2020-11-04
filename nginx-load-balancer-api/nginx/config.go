/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nginx

import (
	"github.com/usmanager/manager/nginx-load-balancer-api/util/cmd"
	"github.com/usmanager/manager/nginx-load-balancer-api/util/files"
	"html/template"
	"log"
	"os"
	"path/filepath"
	"sync"

	"github.com/usmanager/manager/nginx-load-balancer-api/data"
)

const generatedPath = "generated"
const filePath = generatedPath + "/nginx.conf"
const dstPath = "/usr/local/nginx/conf/nginx.conf"

const tmpl = `load_module "modules/ngx_http_geoip2_module.so";

user nginx;
worker_processes auto;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;

events {
	worker_connections 1024;
}

http {
	geoip2 /usr/local/share/GeoIP/GeoLite2-City.mmdb {
		$geoip2_location_latitude default=-1 location latitude;
		$geoip2_location_longitude default=-1 location longitude;
    }
	{{range $service, $servers := .}}
	upstream {{$service}} {
		least_conn; {{range $servers}}
        server {{.Server}}; #{{.Latitude}} {{.Longitude}} {{.Region}}{{end}}
	}
	{{end}}
  	server {
		listen 80;
		server_name load-balancer.com;
		include /etc/nginx/conf.d/*.conf;
		{{range $service, $servers := .}}
		location /{{$service}}/ {
      		#proxy_connect_timeout 100;
      		#proxy_read_timeout 100;
      		proxy_pass http://{{$service}}/;
      		proxy_set_header X-Latitude $geoip2_location_latitude;
      		proxy_set_header X-Longitude $geoip2_location_longitude;
    	}
		{{end}}
    	location /_/api/ {
      		#proxy_connect_timeout 100;
      		#proxy_read_timeout 100;

      		auth_basic "Restricted";
      		auth_basic_user_file /etc/nginx/.htpasswd;

            proxy_pass http://localhost:1906/api/;
      
      		proxy_set_header X-Forwarded-Host $host;
      		proxy_set_header Authorization "";
      		proxy_redirect off;

      		proxy_set_header X-Latitude $geoip2_location_latitude;
      		proxy_set_header X-Longitude $geoip2_location_longitude;
    	}
	}
}
`

func UpdateNginx() {
	log.Print("Updating nginx")
	go generateNginxConfigFile()
}

func generateNginxConfigFile() {
	log.Print("Updating nginx config files")
	folderAbsPath, _ := filepath.Abs(generatedPath)
	fileAbsPath, _ := filepath.Abs(filePath)
	copyAbsPath, _ := filepath.Abs(dstPath)
	t := template.New("Nginx configuration file")
	t, err := t.Parse(tmpl)

	if err != nil {
		log.Fatal("Parse: ", err)
		return
	}

	if _, err := os.Stat(folderAbsPath); os.IsNotExist(err) {
		_ = os.Mkdir(folderAbsPath, 0755)
	}

	f, err := os.Create(fileAbsPath)
	if err != nil {
		log.Println("Create file: ", err)
		return
	}

	err = t.Execute(f, data.Servers)
	if err != nil {
		log.Fatal("Execute: ", err)
		return
	}
	_ = f.Close()
	err = files.Copy(fileAbsPath, copyAbsPath)
	if err != nil {
		log.Println("Copying file failed ", err)
	} else {
		log.Println("Copying file succeeded")
		reloadNginx()
	}
}

func reloadNginx() {
	wg := new(sync.WaitGroup)
	wg.Add(1)
	cmd.Execute("nginx -s reload", wg)
	wg.Wait()
}
