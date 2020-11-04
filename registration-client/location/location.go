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
	"github.com/usmanager/manager/registration-client/data"
	"github.com/usmanager/manager/registration-client/instance"
	"net/http"
	"sync"
	"time"

	"github.com/usmanager/manager/registration-client/reglog"
)

var lock sync.Mutex
var locationRequests *ConcurrentMap


func init() {
	locationRequests = NewConcurrentMap()
}

func RegisterRequest(service string) {
	locationRequest := data.LocationRequest{
		Service: service,
		Count: 1,
	}
	AddRequest(locationRequest)
}

func AddRequest(locationRequest data.LocationRequest) {
	lock.Lock()
	defer lock.Unlock()
	serviceRequests, hasRequests := locationRequests.Get(locationRequest.Service)
	count := 1
	if hasRequests && serviceRequests != nil {
		count += serviceRequests.(data.LocationRequest).Count
	}
	newServiceRequests := data.LocationRequest{
		Service:   locationRequest.Service,
		Count:     count,
	}
	locationRequests.Set(locationRequest.Service, newServiceRequests)
}

func clear(serviceKey string) {
	lock.Lock()
	defer lock.Unlock()
	locationRequests.Set(serviceKey, nil)
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
			case <-stopChan:
				return
			}
		}

	}(sendTicker)

	return stopChan
}

func sendAllData() {
	for mapItem := range locationRequests.Iter() {
		serviceKey := mapItem.Key
		value := mapItem.Value
		if value != nil {
			serviceRequests := value.(data.LocationRequest)
			if serviceRequests.Count > 0 {
				go sendData(serviceRequests)
				go clear(serviceKey)
			}
		}

	}
}

func sendData(data data.LocationRequest) {
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
