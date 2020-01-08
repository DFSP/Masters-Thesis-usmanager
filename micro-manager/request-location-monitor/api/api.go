package api

import (
	"encoding/json"
	"flag"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/gorilla/mux"
	"bitbucket.org/microservicemanagement/request-location-monitor/utils"
	"bitbucket.org/microservicemanagement/request-location-monitor/data"
)

var defaultInterval = 60
var defaultIntervalParam string
var lock sync.Mutex

func init() {
	flag.StringVar(&defaultIntervalParam, "interval", "60", "Default interval on list top monitoring")
	intervalParam, err := strconv.Atoi(defaultIntervalParam)
	if err == nil {
		defaultInterval = intervalParam
	}
}

// ListAllMonitoring returns monitoring data from all services
func ListAllMonitoring(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	locationMonitoringResps := []data.LocationMonitoringRespAll{}

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceName := mapItem.Key
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationMonitoring)
			serviceResp := data.LocationMonitoringRespAll{
				ToService:    serviceName,
				LocationData: monitoringData,
			}
			locationMonitoringResps = append(locationMonitoringResps, serviceResp)
		}
	}

	json.NewEncoder(w).Encode(locationMonitoringResps)
}

// ListAllMonitoringTop returns monitoring data from all services agg
func ListAllMonitoringTop(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	seconds := defaultInterval
	vars := mux.Vars(r)
	secondsParam, hasParam := vars["seconds"]
	if hasParam {
		secondsParamInt, err := strconv.Atoi(secondsParam)
		if err == nil {
			seconds = secondsParamInt
		}
	}

	maxTime := time.Now()
	minTime := maxTime.Add(time.Duration(-seconds) * time.Second)

	locationMonitoringAggTemp := make(map[string]map[string]data.LocationMonitoringSimple)
	locationMonitoringResps := []data.LocationMonitoringResp{}

	for mapItem := range data.LocationMonitoringData.Iter() {
		serviceName := mapItem.Key
		serviceData := mapItem.Value.(*utils.ConcurrentSlice)
		locationMonitoringAggTemp[serviceName] = make(map[string]data.LocationMonitoringSimple)
		for sliceItem := range serviceData.Iter() {
			monitoringData := sliceItem.Value.(data.LocationMonitoring)
			if monitoringData.Timestamp.After(minTime) && monitoringData.Timestamp.Before(maxTime) {
				continent := monitoringData.FromContinent
				region := monitoringData.FromRegion
				country := monitoringData.FromCountry
				city := monitoringData.FromCity
				locationKey := continent + "_" + region + "_" + country + "_" + city
				serviceLocationAgg, hasLocation := locationMonitoringAggTemp[serviceName][locationKey]
				if hasLocation {
					newCount := serviceLocationAgg.Count + monitoringData.Count
					locationMonitoringAggTemp[serviceName][locationKey] = data.LocationMonitoringSimple{
						FromContinent: continent,
						FromRegion:    region,
						FromCountry:   country,
						FromCity:      city,
						ToService:     serviceName,
						Count:         newCount,
					}
				} else { //Location is not on agg
					locationMonitoringAggTemp[serviceName][locationKey] = data.LocationMonitoringSimple{
						FromContinent: continent,
						FromRegion:    region,
						FromCountry:   country,
						FromCity:      city,
						ToService:     serviceName,
						Count:         monitoringData.Count,
					}
				}
			}
		}
	}
	for serviceName, serviceDataAgg := range locationMonitoringAggTemp {
		for _, monitoringDataAgg := range serviceDataAgg {

			serviceAggResp := data.LocationMonitoringResp{
				ToService:    serviceName,
				LocationData: monitoringDataAgg,
			}
			locationMonitoringResps = append(locationMonitoringResps, serviceAggResp)
		}
	}

	json.NewEncoder(w).Encode(locationMonitoringResps)
}

// AddMonitoringData adds new monitoring data
// Request example : {"hostname": "server1:8080"}
func AddMonitoringData(w http.ResponseWriter, r *http.Request) {
	var requestMonitoringData data.LocationMonitoringSimple
	_ = json.NewDecoder(r.Body).Decode(&requestMonitoringData)

	newMonitoringData := data.LocationMonitoring{
		FromContinent: requestMonitoringData.FromContinent,
		FromRegion:    requestMonitoringData.FromRegion,
		FromCountry:   requestMonitoringData.FromCountry,
		FromCity:      requestMonitoringData.FromCity,
		ToService:     requestMonitoringData.ToService,
		Count:         requestMonitoringData.Count,
		Timestamp:     time.Now(),
	}

	addMonitoringData(newMonitoringData)

	var msg data.Message
	msg.Message = "success"
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(msg)
}

func addMonitoringData(newMonitoringData data.LocationMonitoring) {
	lock.Lock()
	defer lock.Unlock()
	serviceMonitoringDataSlice, ok := data.LocationMonitoringData.Get(newMonitoringData.ToService)
	if ok {
		serviceMonitoringDataSlice.(*utils.ConcurrentSlice).Append(newMonitoringData)
		data.LocationMonitoringData.Set(newMonitoringData.ToService, serviceMonitoringDataSlice)
	} else {
		newServiceMonitoringDataSlice := utils.NewConcurrentSlice()
		newServiceMonitoringDataSlice.Append(newMonitoringData)
		data.LocationMonitoringData.Set(newMonitoringData.ToService, newServiceMonitoringDataSlice)
	}
}
