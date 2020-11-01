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
	"time"

	"github.com/usmanager/manager/nginx-load-balancer-api/data"
	"github.com/usmanager/manager/nginx-load-balancer-api/nginx"
)

var delay int

func init() {
	flag.IntVar(&delay, "delay", 15, "Update delay (in seconds) of the nginx configuration after adding a new server")
}

func GetServers(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	if len(data.Servers) == 0 {
		response := make([]string, 0)
		json.NewEncoder(w).Encode(response)
		log.Print("Looking for servers... found none")
	} else {
		json.NewEncoder(w).Encode(data.Servers)
		log.Printf("Looking for servers... found %+v", data.Servers)
	}
}

func AddServers(w http.ResponseWriter, r *http.Request) {
	var reply []data.ServerWeight

	var servers []data.Server
	_ = json.NewDecoder(r.Body).Decode(&servers)

	for _, server := range servers {
		data.Servers = append(data.Servers, server)
		serverWeight := CalculateWeight(server)
		data.ServersWeight = append(data.ServersWeight, serverWeight)
		reply = append(reply, serverWeight)
		log.Printf("Added server %+v with weight %d", server, serverWeight.Weight)
	}

	time.AfterFunc(time.Duration(delay) * time.Second, func() {
		nginx.UpdateNginx()
	})

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(reply)
}

func DeleteServer(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	hostname := vars["hostname"]

	removed := false
	for index, server := range data.Servers {
		if server.Hostname == hostname {
			data.Servers = append(data.Servers[:index], data.Servers[index+1:]...)
			log.Printf("Removed server %+v", server)
			removed = true
			break
		}
	}

	if removed {
		nginx.UpdateNginx()
	} else {
		log.Printf("Server %s not found", hostname)
		w.WriteHeader(http.StatusNotFound)
	}
}

func CalculateWeight(server data.Server) data.ServerWeight {
	var distance uint16 = 0

	return data.ServerWeight{
		Hostname: server.Hostname,
		Weight: distance,
	}
}