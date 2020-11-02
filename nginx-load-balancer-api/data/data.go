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
)

type Server struct {
	Server    string  `json:"server"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Region    string  `json:"region"`
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

	// process initial server, if any
	var server Server
	var serverJson = os.Getenv("server")
	if len(serverJson) > 0 {
		_ = json.Unmarshal([]byte(serverJson), &server)
		Servers[server.Server] = []Server{server}
		log.Printf("Added server %+v", server)
	}
}
