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
	"fmt"
	"github.com/usmanager/manager/request-location-monitor/reglog"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/usmanager/manager/request-location-monitor/data"
	"github.com/usmanager/manager/request-location-monitor/utils"
)

var interval int

var lock sync.Mutex

func init() {
	flag.IntVar(&interval, "interval", 60000, "Default interval (in miliseconds) to include instances on data aggregation")
}

func ListLocationRequests(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	vars := r.URL.Query()
	if _, aggregation := vars["aggregation"]; aggregation {
		listMonitoringAggregation(w, r)
	} else {
		listMonitoring(w)
	}
}

func listMonitoring(w http.ResponseWriter) {
	locationMonitoring := []data.LocationRequest{}

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationRequest)
			locationMonitoring = append(locationMonitoring, monitoringData)
		}
	}

	reglog.Logger.Infof("Replying locationMonitoring with: %+v", locationMonitoring)
	_ = json.NewEncoder(w).Encode(locationMonitoring)
}

func listMonitoringAggregation(w http.ResponseWriter, r *http.Request) {
	vars := r.URL.Query()

	aggregationInterval := interval
	intervalQuery := vars.Get("interval")
	if len(intervalQuery) > 0 {
		overwrittenInterval, err := strconv.Atoi(intervalQuery)
		if err == nil {
			aggregationInterval = overwrittenInterval
		}
	}

	maxTime := time.Now()
	minTime := maxTime.Add(time.Duration(-aggregationInterval) * time.Millisecond)

	serviceLocationMonitoring := make(map[string]int)

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceName := mapItem.Key
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationRequest)
			if monitoringData.Timestamp.Before(minTime) || monitoringData.Timestamp.After(maxTime) {
				continue
			}
			count := monitoringData.Count
			serviceCount, hasService := serviceLocationMonitoring[serviceName]
			if hasService {
				count += serviceCount
			}

			serviceLocationRequest := data.ServiceLocationRequest{
				Count:       count,
				Latitude:    monitoringData.Latitude,
				Longitude:   monitoringData.Longitude,
			}

			serviceLocationMonitoring[serviceName] = serviceLocationRequest
		}
	}

	reglog.Logger.Infof("Replying serviceLocationMonitoring with: %+v", serviceLocationMonitoring)
	_ = json.NewEncoder(w).Encode(serviceLocationMonitoring)
}

func AddLocationRequest(w http.ResponseWriter, r *http.Request) {
	var requestMonitoringData data.LocationRequest
	_ = json.NewDecoder(r.Body).Decode(&requestMonitoringData)
	monitoringData := data.LocationRequest{
		Service:   requestMonitoringData.Service,
		Count:     requestMonitoringData.Count,
		Latitude:  requestMonitoringData.Latitude,
		Longitude: requestMonitoringData.Longitude,
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
		reglog.Logger.Infof("Added location request to existing service: %s = %s", service, string(monitoringDataJson))
	} else {
		serviceMonitoringDataSlice := utils.NewConcurrentSlice()
		serviceMonitoringDataSlice.Append(monitoringData)
		data.LocationMonitoringData.Set(service, serviceMonitoringDataSlice)
		reglog.Logger.Infof("Added location request to new service: %s = %s", service, string(monitoringDataJson))
	}
}
