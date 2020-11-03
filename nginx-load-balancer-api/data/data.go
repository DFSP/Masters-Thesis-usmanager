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

package data

import (
	"encoding/json"
	"log"
	"os"
	"sync"
)

type Server struct {
	Server    string  `json:"server"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Region    string  `json:"region"`
}

type ServiceServers struct {
	Service string   `json:"service"`
	Servers []Server `json:"servers"`
}

type Coordinates struct {
	Label     string  `json:"label,omitempty"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

type Location struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Region    string  `json:"region"`
}

var Servers map[string][]Server
var LoadBalancerLocation Location

var lock sync.Mutex

func init() {
	Servers = make(map[string][]Server)

	// set loadbalancer location
	var coordinates Coordinates
	_ = json.Unmarshal([]byte(os.Getenv("coordinates")), &coordinates)
	LoadBalancerLocation = Location{
		Latitude:  coordinates.Latitude,
		Longitude: coordinates.Longitude,
		Region:    os.Getenv("region"),
	}

	// process initial servers, if any
	var servers []ServiceServers
	var serversJson = os.Getenv("servers")
	if len(serversJson) > 0 {
		err := json.Unmarshal([]byte(serversJson), &servers)
		if err != nil {
			log.Printf("Failed to process initial servers, %s", err)
		} else {
			for _, s := range servers {
				service := s.Service
				servers := s.Servers
				AddServiceServers(service, servers)
				Servers[service] = servers
				log.Printf("Added servers %+v to service %v", servers, service)
			}
		}
	}
}

func AddServiceServers(service string, servers []Server) bool {
	var success = false

	lock.Lock()
	defer lock.Unlock()

	for _, server := range servers {
		currentServers, hasServers := Servers[service]
		if HasServer(server) {
			log.Printf("Server %v is already registered to service %s", server, service)
		} else if !hasServers {
			Servers[service] = []Server{server}
			success = true
			log.Printf("Added first server %+v to service %s", server, service)
		} else {
			currentServers = append(currentServers, server)
			Servers[service] = currentServers
			success = true
			log.Printf("Added server %+v to service %s, current servers %v", server, service, currentServers)
		}
	}

	return success
}

func DeleteServer(service string, server string) bool {
	deleted := false

	servers, hasServers := Servers[service]
	if hasServers {
		for index, s := range servers {
			if s.Server == server {
				servers = append(servers[:index], servers[index+1:]...)
				if len(servers) > 0 {
					Servers[service] = servers
					log.Printf("Removed server %+v from service %s, current servers %v", server, service, servers)
				} else {
					delete(Servers, service)
					log.Printf("Removed server %+v from service %s, no more servers", server, service)
				}
				deleted = true
				break
			}
		}
	}

	return deleted
}

func HasServer(server Server) bool {
	for _, servers := range Servers {
		for _, s := range servers {
			if server.Server == s.Server {
				return true
			}
		}
	}
	return false
}