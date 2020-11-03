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

package api

import (
	"encoding/json"
	"flag"
	"github.com/gorilla/mux"
	"log"
	"net/http"
	"sync"
	"time"

	"github.com/usmanager/manager/nginx-load-balancer-api/data"
	"github.com/usmanager/manager/nginx-load-balancer-api/nginx"
)

var lock sync.Mutex

var delay int

func init() {
	flag.IntVar(&delay, "delay", 15, "Update delay (in seconds) of the nginx configuration after adding a new server")
}

func GetServers(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	serviceServers := make([]data.ServiceServers, 0)
	for service, servers := range data.Servers {
		serviceServer := data.ServiceServers{
			Service: service,
			Servers: servers,
		}
		serviceServers = append(serviceServers, serviceServer)
	}

	_ = json.NewEncoder(w).Encode(serviceServers)
}

func GetServiceServers(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	vars := mux.Vars(r)
	service := vars["service"]

	lock.Lock()
	defer lock.Unlock()

	services, hasServices := data.Servers[service]
	if hasServices {
		_ = json.NewEncoder(w).Encode(services)
		log.Printf("Looking for servers of %s... found %+v", service, services)
	} else {
		response := make([]string, 0)
		_ = json.NewEncoder(w).Encode(response)
		log.Printf("Looking for servers of %s... found none", service)
	}
}

func AddServiceServers(w http.ResponseWriter, r *http.Request) {
	var servers []data.Server
	err := json.NewDecoder(r.Body).Decode(&servers)
	if err != nil {
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode("Invalid json")
		return
	}

	vars := mux.Vars(r)
	service := vars["service"]

	if service == "_" {
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode("Invalid service")
		return
	}

	if data.AddServiceServers(service, servers) {
		time.AfterFunc(time.Duration(delay)*time.Second, func() {
			nginx.UpdateNginx()
		})
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(servers)
	} else {
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode("Servers already registered")
	}
}

func DeleteServiceServer(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	service := vars["service"]
	server := vars["server"]

	lock.Lock()
	defer lock.Unlock()

	if data.DeleteServiceServer(service, server) {
		nginx.UpdateNginx()
	} else {
		log.Printf("Server %s of service %s was not found", server, service)
		w.WriteHeader(http.StatusNotFound)
	}
}
