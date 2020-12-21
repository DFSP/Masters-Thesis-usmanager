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
	"github.com/usmanager/manager/registration-client/data"
	eureka "github.com/usmanager/manager/registration-client/eurekaops"
	"github.com/usmanager/manager/registration-client/instance"
	"github.com/usmanager/manager/registration-client/location"
	"github.com/usmanager/manager/registration-client/reglog"
	"net/http"
	"strings"
	"time"
)

var cache time.Duration

var serviceInstances map[string][]*eureka.Instance
var serviceInstancesUpdate map[string]time.Time

func init() {
	cache = time.Duration(*flag.Int("cache", 10000, "Time (in ms) to cache instances endpoints before contacting Eureka"))

	serviceInstances = make(map[string][]*eureka.Instance)
	serviceInstancesUpdate = make(map[string]time.Time)
}

func GetServiceEndpoint(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	vars := mux.Vars(r)
	service := strings.ToLower(vars["service"])

	go location.RegisterRequest(service)

	var errorMessage string
	var instanceEndpoint data.InstanceEndpoint

	instances, hasServiceInstances := serviceInstances[service]
	if hasServiceInstances {
		if serviceInstancesUpdate[service].Add(cache).After(time.Now()) {
			instanceEndpoint = eureka.GetBestInstance(&instance.Instance, instances)
		} else {
			instances, err := instance.EurekaServer.GetInstancesByVIPAddress(service, false, eureka.ThatAreUp)
			if err == nil {
				if len(instances) > 0 {
					instanceEndpoint = eureka.GetBestInstance(&instance.Instance, instances)
					serviceInstances[service] = instances
					serviceInstancesUpdate[service] = time.Now()
				}
			} else {
				errorMessage = err.Error()
			}
		}
	}

	if len(errorMessage) > 0 {
		w.WriteHeader(http.StatusBadGateway)
		_ = json.NewEncoder(w).Encode(errorMessage)
		reglog.Logger.Errorf("Error getting instances from eureka: %s", errorMessage)
	} else if len(instanceEndpoint.InstanceId) == 0 {
		reglog.Logger.Infof("Found no instances")
		w.WriteHeader(http.StatusNotFound)
	} else {
		reglog.Logger.Infof("Found instances: %s", instances)
		reglog.Logger.Infof("Instance chosen for %s: %s", service, instanceEndpoint.InstanceId)
		_ = json.NewEncoder(w).Encode(instanceEndpoint)
	}

}

func GetServiceEndpoints(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	vars := mux.Vars(r)
	service := strings.ToLower(vars["service"])

	var errorMessage string
	var instanceEndpoints []data.InstanceEndpoint

	instances, err := instance.EurekaServer.GetInstancesByVIPAddress(service, false, eureka.ThatAreUp)
	if err == nil {
		for _, serviceInstance := range instances {
			instanceEndpoint := data.InstanceEndpoint{
				InstanceId: serviceInstance.InstanceId,
				Endpoint:   serviceInstance.HomePageUrl,
			}
			instanceEndpoints = append(instanceEndpoints, instanceEndpoint)
		}
	} else {
		errorMessage = err.Error()
	}

	if len(errorMessage) > 0 {
		w.WriteHeader(http.StatusBadGateway)
		_ = json.NewEncoder(w).Encode(errorMessage)
		reglog.Logger.Errorf("Error getting instances from eureka: %s", errorMessage)
	} else {
		reglog.Logger.Infof("Found instances: %s", instanceEndpoints)
		_ = json.NewEncoder(w).Encode(instanceEndpoints)
	}
}

func RegisterServiceEndpoint(w http.ResponseWriter, r *http.Request) {
	reglog.Logger.Infof("Registering server by request")
	go func() {
		for i := 0; i < 5; i++ {
			err := instance.Register()
			if err == nil {
				break
			}
			reglog.Logger.Error(err)
			time.Sleep(5 * time.Second)
		}
	}()
}

func RegisterLocationMonitoring(w http.ResponseWriter, r *http.Request) {
	var locationMonitoring data.LocationRequest
	err := json.NewDecoder(r.Body).Decode(&locationMonitoring)
	if err != nil {
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode("Invalid json")
	} else {
		go location.AddRequest(locationMonitoring)
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		_ = json.NewEncoder(w).Encode(locationMonitoring)
	}
}
