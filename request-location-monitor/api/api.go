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
	"log"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/usmanager/manager/request-location-monitor/data"
	"github.com/usmanager/manager/request-location-monitor/utils"
)

var defaultInterval = 60
var lock sync.Mutex

func init() {
	var defaultIntervalParam string
	flag.StringVar(&defaultIntervalParam, "interval", "60", "Default interval on list top monitoring")
	intervalParam, err := strconv.Atoi(defaultIntervalParam)
	if err == nil {
		defaultInterval = intervalParam
	}
}

func ListMonitoring(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	vars := r.URL.Query()
	if _, aggregation := vars["aggregation"]; aggregation {
		listMonitoringAggregation(w, r)
	} else {
		listMonitoring(w)
	}
}

func listMonitoring(w http.ResponseWriter) {
	locationMonitoring := []data.LocationMonitoring{}

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationMonitoring)
			locationMonitoring = append(locationMonitoring, monitoringData)
		}
	}

	json.NewEncoder(w).Encode(locationMonitoring)
}

func listMonitoringAggregation(w http.ResponseWriter, r *http.Request) {
	vars := r.URL.Query()

	interval, err := strconv.Atoi(vars.Get("interval"))
	if err != nil {
		interval = defaultInterval
	}

	maxTime := time.Now()
	minTime := maxTime.Add(time.Duration(-interval) * time.Second)

	serviceLocationMonitoring := make(map[string]map[string]data.ServiceLocationMonitoring)

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceName := mapItem.Key
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		serviceLocationMonitoring[serviceName] = make(map[string]data.ServiceLocationMonitoring)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationMonitoring)
			if monitoringData.Timestamp.Before(minTime) || monitoringData.Timestamp.After(maxTime) {
				continue
			}
			continent := monitoringData.Continent
			region := monitoringData.Region
			country := monitoringData.Country
			city := monitoringData.City
			count := monitoringData.Count
			locationKey := continent + "_" + region + "_" + country + "_" + city
			serviceData, hasLocation := serviceLocationMonitoring[serviceName][locationKey]
			if hasLocation {
				count += serviceData.Count
			}
			serviceLocationMonitoring[serviceName][locationKey] = data.ServiceLocationMonitoring{
				Continent: continent,
				Region:    region,
				Country:   country,
				City:      city,
				Service:   serviceName,
				Count:     count,
			}
		}
	}

	servicesLocationMonitoring := []data.ServiceLocationMonitoring{}
	for _, serviceData := range serviceLocationMonitoring {
		for _, serviceMonitoring := range serviceData {
			servicesLocationMonitoring = append(servicesLocationMonitoring, serviceMonitoring)
		}
	}

	json.NewEncoder(w).Encode(servicesLocationMonitoring)
}

func AddMonitoring(w http.ResponseWriter, r *http.Request) {
	var requestMonitoringData data.ServiceLocationMonitoring
	_ = json.NewDecoder(r.Body).Decode(&requestMonitoringData)
	monitoringData := data.LocationMonitoring{
		Continent: requestMonitoringData.Continent,
		Region:    requestMonitoringData.Region,
		Country:   requestMonitoringData.Country,
		City:      requestMonitoringData.City,
		Service:   requestMonitoringData.Service,
		Count:     requestMonitoringData.Count,
		Timestamp: time.Now(),
	}
	monitoringDataJson, _ := json.Marshal(monitoringData)
	service := monitoringData.Service

	lock.Lock()
	defer lock.Unlock()
	serviceMonitoringDataSlice, ok := data.LocationMonitoringData.Get(service)
	if ok {
		serviceMonitoringDataSlice.(*utils.ConcurrentSlice).Append(monitoringData)
		data.LocationMonitoringData.Set(service, serviceMonitoringDataSlice)
		log.Printf("Added location monitoring to existing service %s: %s", service, string(monitoringDataJson))
	} else {
		serviceMonitoringDataSlice := utils.NewConcurrentSlice()
		serviceMonitoringDataSlice.Append(monitoringData)
		data.LocationMonitoringData.Set(service, serviceMonitoringDataSlice)
		log.Printf("Added location monitoring to new service %s: %s", service, string(monitoringDataJson))
	}
}
