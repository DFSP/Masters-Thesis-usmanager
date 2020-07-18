/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package locationutils

import (
	"bytes"
	"encoding/json"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/usmanager/manager/register-go/api"
	"github.com/usmanager/manager/register-go/reglog"
)

var sendDataPeriod = 5 * time.Second

var lock sync.Mutex
var hostname string
var fromContinent string
var fromRegion string
var fromCountry string
var fromCity string

// locationMonitoringData data
var locationMonitoringData *ConcurrentMap

//InitLocationData initialize data
func InitLocationData(thisHostname string, thisFromContinent string, thisFromRegion string, thisFromCountry string, thisFromCity string, sendLocationDataPeriod string) {
	customSendLocationDataPeriod, err := strconv.Atoi(sendLocationDataPeriod)
	if err == nil {
		sendDataPeriod = time.Duration(customSendLocationDataPeriod) * time.Second
	}
	hostname = thisHostname
	fromContinent = thisFromContinent
	fromRegion = thisFromRegion
	fromCountry = thisFromCountry
	fromCity = thisFromCity
	locationMonitoringData = NewConcurrentMap()
	go sendDataTimer()
}

//AddLocationData add location data
func AddLocationData(toService string) {
	lock.Lock()
	defer lock.Unlock()
	serviceData, ok := locationMonitoringData.Get(toService)
	if ok {
		currServiceData := serviceData.(api.LocationMonitoring)
		newCount := currServiceData.Count + 1
		newServiceData := api.LocationMonitoring{
			Valid:         true,
			FromContinent: fromContinent,
			FromRegion:    fromRegion,
			FromCountry:   fromCountry,
			FromCity:      fromCity,
			ToService:     toService,
			Count:         newCount,
		}
		locationMonitoringData.Set(toService, newServiceData)
	} else {
		newServiceData := api.LocationMonitoring{
			Valid:         true,
			FromContinent: fromContinent,
			FromRegion:    fromRegion,
			FromCountry:   fromCountry,
			FromCity:      fromCity,
			ToService:     toService,
			Count:         1,
		}
		locationMonitoringData.Set(toService, newServiceData)
	}
}

//AddCustomLocationData add custom location data
func AddCustomLocationData(fromContinent string, fromCountry string, fromCity, toService string) {
	lock.Lock()
	defer lock.Unlock()
	locationKey := fromContinent + "_" + fromCountry + "_" + fromCity
	serviceData, ok := locationMonitoringData.Get(toService + locationKey)
	if ok {
		currServiceData := serviceData.(api.LocationMonitoring)
		newCount := currServiceData.Count + 1
		newServiceData := api.LocationMonitoring{
			Valid:         true,
			FromContinent: fromContinent,
			FromRegion:    "none",
			FromCountry:   fromCountry,
			FromCity:      fromCity,
			ToService:     toService,
			Count:         newCount,
		}
		locationMonitoringData.Set(toService+locationKey, newServiceData)
	} else {
		newServiceData := api.LocationMonitoring{
			Valid:         true,
			FromContinent: fromContinent,
			FromRegion:    "none",
			FromCountry:   fromCountry,
			FromCity:      fromCity,
			ToService:     toService,
			Count:         1,
		}
		locationMonitoringData.Set(toService+locationKey, newServiceData)
	}
}

func clearLocationData(serviceKey string) {
	lock.Lock()
	defer lock.Unlock()
	newServiceData := api.LocationMonitoring{
		Valid:         false,
		FromContinent: "",
		FromRegion:    "",
		FromCountry:   "",
		FromCity:      "",
		ToService:     "",
		Count:         0,
	}
	locationMonitoringData.Set(serviceKey, newServiceData)
}

func sendAllMonitoringData(timer time.Time) {
	count := 0
	for mapItem := range locationMonitoringData.Iter() {
		serviceKey := mapItem.Key
		serviceData := mapItem.Value.(api.LocationMonitoring)
		if serviceData.Count > 0 {
			count = count + serviceData.Count
			go sendMonitorindData(serviceData.FromContinent, serviceData.FromRegion, serviceData.FromCountry, serviceData.FromCity, serviceData.ToService, serviceData.Count)
			go clearLocationData(serviceKey)
		}
	}
	if count > 0 {
		reglog.Logger.Infof("Sended all location data. Started at %s and finished at %s", timer.String(), time.Now().String())
	}
}

func sendMonitorindData(fromContinent string, fromRegion string, fromCountry string, fromCity string, toService string, count int) {
	url := "http://" + hostname + ":1919/api/monitoringinfo/add"
	values := api.LocationMonitoringSimple{
		FromContinent: fromContinent,
		FromRegion:    fromRegion,
		FromCountry:   fromCountry,
		FromCity:      fromCity,
		ToService:     toService,
		Count:         count,
	}
	jsonValue, _ := json.Marshal(values)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonValue))
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
}

func sendDataTimer() {
	time.Sleep(sendDataPeriod)
	c := time.Tick(sendDataPeriod)
	for sendDataTime := range c {
		sendAllMonitoringData(sendDataTime)
	}
}
