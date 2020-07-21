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
	"net/http"
	"time"

	"github.com/usmanager/manager/nginx-load-balancer-api/data"
	"github.com/usmanager/manager/nginx-load-balancer-api/nginx"
)

const secondsDelayAddServer = 15

// GetServers return all servers.
func GetServers(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	if len(data.Servers) == 0 {
		response := make([]string, 0)
		json.NewEncoder(w).Encode(response)
	} else {
		json.NewEncoder(w).Encode(data.Servers)
	}
}

// GetOtherRegionsServers return other regions servers.
func GetOtherRegionsServers(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	if len(data.OtherRegionsServers) == 0 {
		response := make([]string, 0)
		json.NewEncoder(w).Encode(response)
	} else {
		json.NewEncoder(w).Encode(data.OtherRegionsServers)
	}
}

// GetSameRegionServers return same region servers as load balancer.
func GetSameRegionServers(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	if len(data.SameRegionsServers) == 0 {
		response := make([]string, 0)
		json.NewEncoder(w).Encode(response)
	} else {
		json.NewEncoder(w).Encode(data.SameRegionsServers)
	}
}

// AddServer adds a new server.
// Request example : [{"hostname": "server1:8080"}, {"hostname": "server2:8181"}]
func AddServer(w http.ResponseWriter, r *http.Request) {
	var serversToAdd []data.Server
	_ = json.NewDecoder(r.Body).Decode(&serversToAdd)

	for _, item := range serversToAdd {
		serverW := data.GetServerWeight(item)
		if item.Region == data.LoadBalancerLocation.Region {
			data.SameRegionsServers = append(data.SameRegionsServers, serverW)
		} else {
			data.OtherRegionsServers = append(data.OtherRegionsServers, serverW)
		}
	}

	updateServers()

	var msg data.Message
	msg.Message = "success"
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(msg)
	updateNginx()
}

// DeleteServer deletes a server.
// Request example : {"hostname": "server1:8080"}
func DeleteServer(w http.ResponseWriter, r *http.Request) {
	var serverToDelete data.SimpleServer
	_ = json.NewDecoder(r.Body).Decode(&serverToDelete)

	var found = false

	for index, item := range data.SameRegionsServers {
		if item.Hostname == serverToDelete.Hostname {
			data.SameRegionsServers = append(data.SameRegionsServers[:index], data.SameRegionsServers[index+1:]...)
			found = true
			break
		}
	}

	if !found {
		for index, item := range data.OtherRegionsServers {
			if item.Hostname == serverToDelete.Hostname {
				data.OtherRegionsServers = append(data.OtherRegionsServers[:index], data.OtherRegionsServers[index+1:]...)
				found = true
				break
			}
		}
	}

	updateServers()

	var msg data.Message
	msg.Message = "success"
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(msg)
	go nginx.UpdateNginx()
}

func updateNginx() {
	time.AfterFunc(secondsDelayAddServer*time.Second, func() {
		nginx.UpdateNginx()
	})
}

func updateServers() {
	if len(data.SameRegionsServers) == 0 {
		data.Servers = data.OtherRegionsServers
	} else {
		data.Servers = data.SameRegionsServers
		if len(data.OtherRegionsServers) > 0 {
			data.Servers = append(data.Servers, data.OtherRegionsServers...)
		}
	}
}
