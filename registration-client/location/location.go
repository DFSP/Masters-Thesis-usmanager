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
	"flag"
	"fmt"
	"github.com/usmanager/manager/registration-client/data"
	"github.com/usmanager/manager/registration-client/instance"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/usmanager/manager/registration-client/reglog"
)

var lock sync.Mutex
var locationMonitoringData *ConcurrentMap

func init() {
	locationMonitoringData = NewConcurrentMap()

	interval := flag.Int("interval", 5000, "Interval time (in ms) to send location data")
	go sendTimer(time.Duration(*interval))
}

func RegisterRequest(service string) {
	lock.Lock()
	defer lock.Unlock()
	serviceData, hasServiceData := locationMonitoringData.Get(service)
	count := 1
	if hasServiceData {
		count += serviceData.(data.LocationMonitoring).Count
	}
	newServiceData := data.LocationMonitoring{
		Service:   service,
		Latitude:  strconv.FormatFloat(instance.Latitude, 'f', -1, 64),
		Longitude: strconv.FormatFloat(instance.Longitude, 'f', -1, 64),
		Count:     count,
	}
	locationMonitoringData.Set(service, newServiceData)
}

func AddRequest(locationMonitoring data.LocationMonitoring) {
	lock.Lock()
	defer lock.Unlock()
	serviceKey := fmt.Sprintf("%s_%s_%s", locationMonitoring.Service, locationMonitoring.Latitude, locationMonitoring.Longitude)
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

func sendTimer(sendInterval time.Duration) {
	time.Sleep(sendInterval)

	sendTicker := time.NewTicker(sendInterval)
	defer sendTicker.Stop()

	go func() {
		for {
			select {
			case timer := <-sendTicker.C:
				sendAllData(timer)
			}
		}
	}()
}

func sendAllData(timer time.Time) {
	count := 0
	for mapItem := range locationMonitoringData.Iter() {
		serviceKey := mapItem.Key
		serviceData := mapItem.Value.(data.LocationMonitoring)
		if serviceData.Count > 0 {
			count = count + serviceData.Count
			go sendData(serviceData)
			go clear(serviceKey)
		}
	}
	if count > 0 {
		reglog.Logger.Infof("Sent all location data. Started at %s and finished at %s", timer.String(), time.Now().String())
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
		reglog.Logger.Infof("Sent location data %s", data)
	}
}
