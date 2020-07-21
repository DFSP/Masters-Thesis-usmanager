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
	"fmt"
	"html/template"
	"io"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"

	"github.com/usmanager/manager/nginx-load-balancer-api/data"
)

const generatedPath = "/nginx-api-generated"
const filePath = generatedPath + "/nginx.conf"
const copyPath = "/etc/nginx/nginx.conf"

const tmpl = `load_module "modules/ngx_http_geoip_module.so";
user nginx;
worker_processes auto;

error_log /dev/stdout info;
pid /var/run/nginx.pid;

events {
  worker_connections 1024;
}

http {
  geoip_country etc/nginx/geoip/GeoIP.dat;
  geoip_city etc/nginx/geoip/GeoLiteCity.dat;
  
  access_log /dev/stdout;

  upstream myapp {
	least_conn;
    {{- range $index, $server := .}}
    server {{$server.Hostname}} weight={{$server.Weight}};
    {{- end}}
  }

  server {
    listen 80;
    server_name load-balancer.com;

    include /etc/nginx/conf.d/*.conf;

    location / {
      proxy_connect_timeout 100;
      proxy_read_timeout 100;      

      proxy_pass http://myapp;  

      proxy_set_header XCONTINENTCODE $geoip_city_continent_code;
      proxy_set_header XCOUNTRYCODE $geoip_country_code;
      proxy_set_header XCITY $geoip_city;  
	}
    location /_/nginx-load-balancer-api {
	  proxy_connect_timeout 100;
	  proxy_read_timeout 100;

	  auth_basic "Restricted";
	  auth_basic_user_file /etc/nginx/.htpasswd;           

	  proxy_pass http://localhost:1906;
	
	  proxy_set_header X-Forwarded-Host $host;
	  proxy_set_header Authorization "";
	  proxy_redirect off;
	}
  }
}
`

// UpdateNginx updates nginx
func UpdateNginx() {
	if len(data.Servers) == 0 {
		stopNginx()
	} else {
		generateNginxConfigFile()
	}
}

func generateNginxConfigFile() {
	folderAbsPath, _ := filepath.Abs(generatedPath)
	fileAbsPath, _ := filepath.Abs(filePath)
	copyAbsPath, _ := filepath.Abs(copyPath)
	t := template.New("Nginx configuration file")
	t, err := t.Parse(tmpl)

	if err != nil {
		log.Fatal("Parse: ", err)
		return
	}

	if _, err := os.Stat(folderAbsPath); os.IsNotExist(err) {
		os.Mkdir(folderAbsPath, 0755)
	}

	f, err := os.Create(fileAbsPath)
	if err != nil {
		log.Println("create file: ", err)
		return
	}

	err = t.Execute(f, data.Servers)
	if err != nil {
		log.Fatal("Execute: ", err)
		return
	}
	f.Close()
	err = CopyFile(fileAbsPath, copyAbsPath)
	if err != nil {
		log.Println("CopyFile failed ", err)
	} else {
		log.Println("CopyFile succeeded")
		reloadNginx()
	}
}

func reloadNginx() {
	wg := new(sync.WaitGroup)
	wg.Add(1)
	exeCmd("nginx -s reload", wg)
	wg.Wait()
}

func stopNginx() {
	wg := new(sync.WaitGroup)
	wg.Add(1)
	exeCmd("nginx -s stop", wg)
	wg.Wait()
}

// CopyFile copies a file from src to dst. If src and dst files exist, and are
// the same, then return success. Otherwise, attempt to create a hard link
// between the two files. If that fail, copy the file contents from src to dst.
func CopyFile(src, dst string) (err error) {
	sfi, err := os.Stat(src)
	if err != nil {
		return
	}
	if !sfi.Mode().IsRegular() {
		// cannot copy non-regular files (e.g., directories,
		// symlinks, devices, etc.)
		return fmt.Errorf("CopyFile: non-regular source file ", sfi.Name(), sfi.Mode().String())
	}
	dfi, err := os.Stat(dst)
	if err != nil {
		if !os.IsNotExist(err) {
			return
		}
	} else {
		if !(dfi.Mode().IsRegular()) {
			return fmt.Errorf("CopyFile: non-regular destination file %s (%q)", dfi.Name(), dfi.Mode().String())
		}
		if os.SameFile(sfi, dfi) {
			return
		}
	}
	if err = os.Link(src, dst); err == nil {
		return
	}
	err = copyFileContents(src, dst)
	return
}

// copyFileContents copies the contents of the file named src to the file named
// by dst. The file will be created if it does not already exist. If the
// destination file exists, all it's contents will be replaced by the contents
// of the source file.
func copyFileContents(src, dst string) (err error) {
	in, err := os.Open(src)
	if err != nil {
		return
	}
	defer in.Close()
	out, err := os.Create(dst)
	if err != nil {
		return
	}
	defer func() {
		cerr := out.Close()
		if err == nil {
			err = cerr
		}
	}()
	if _, err = io.Copy(out, in); err != nil {
		return
	}
	err = out.Sync()
	return
}

func exeCmd(cmd string, wg *sync.WaitGroup) {
	fmt.Println("command is ", cmd)
	// splitting head => g++ parts => rest of the command
	parts := strings.Fields(cmd)
	head := parts[0]
	parts = parts[1:len(parts)]

	_, err := exec.Command(head, parts...).Output()
	if err != nil {
		log.Printf("%s", err)
	}

	log.Println("Command executed!")

	wg.Done() // Need to signal to waitgroup that this goroutine is done
}
