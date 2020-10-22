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

package location

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/usmanager/manager/registration-client/data"
	"github.com/usmanager/manager/registration-client/instance"
	"net/http"
	"sync"
	"time"

	"github.com/usmanager/manager/registration-client/reglog"
)

var lock sync.Mutex
var locationMonitoringData *ConcurrentMap


func init() {
	locationMonitoringData = NewConcurrentMap()
}

func RegisterRequest(service string) {
	lock.Lock()
	defer lock.Unlock()
	serviceData, hasServiceData := locationMonitoringData.Get(service)
	count := 1
	if hasServiceData && serviceData != nil {
		count += serviceData.(data.LocationMonitoring).Count
	}
	newServiceData := data.LocationMonitoring{
		Service:   service,
		Latitude:  instance.Latitude,
		Longitude: instance.Longitude,
		Count:     count,
	}
	locationMonitoringData.Set(service, newServiceData)
}

func AddRequest(locationMonitoring data.LocationMonitoring) {
	lock.Lock()
	defer lock.Unlock()
	serviceKey := fmt.Sprintf("%s_%f_%f", locationMonitoring.Service, locationMonitoring.Latitude, locationMonitoring.Longitude)
	serviceData, hasServiceData := locationMonitoringData.Get(serviceKey)
	count := 1
	if hasServiceData && serviceData != nil {
		count += serviceData.(data.LocationMonitoring).Count
	}
	newServiceData := data.LocationMonitoring{
		Service:   locationMonitoring.Service,
		Latitude:  locationMonitoring.Latitude,
		Longitude: locationMonitoring.Longitude,
		Count:     count,
	}
	locationMonitoringData.Set(serviceKey, newServiceData)
}

func clear(serviceKey string) {
	lock.Lock()
	defer lock.Unlock()
	locationMonitoringData.Set(serviceKey, nil)
}

func SendLocationTimer(sendInterval time.Duration) chan bool {
	sendTicker := time.NewTicker(sendInterval)

	stopChan := make(chan bool)
	go func(ticker *time.Ticker) {
		defer sendTicker.Stop()

		for {
			select {
			case <-ticker.C:
				sendAllData()
			case stop := <-stopChan:
				if stop {
					return
				}
			}
		}

	}(sendTicker)

	return stopChan
}

func sendAllData() {
	for mapItem := range locationMonitoringData.Iter() {
		serviceKey := mapItem.Key
		value := mapItem.Value
		if value != nil {
			serviceData := value.(data.LocationMonitoring)
			if serviceData.Count > 0 {
				go sendData(serviceData)
				go clear(serviceKey)
			}
		}

	}
}

func sendData(data data.LocationMonitoring) {
	jsonValue, _ := json.Marshal(data)
	req, err := http.NewRequest("POST", instance.RequestLocationMonitorUrl, bytes.NewBuffer(jsonValue))
	if err == nil {
		req.Header.Set("Content-Type", "application/json")
		client := &http.Client{}
		resp, err := client.Do(req)
		if err != nil {
			panic(err)
		}
		defer resp.Body.Close()
		reglog.Logger.Infof("Sent location data %s", jsonValue)
	}
}
