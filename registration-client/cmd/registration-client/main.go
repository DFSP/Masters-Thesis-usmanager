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

package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/gorilla/mux"

	"github.com/usmanager/manager/registration-client/api"
	eureka "github.com/usmanager/manager/registration-client/eurekaops"
	"github.com/usmanager/manager/registration-client/locationutils"
	"github.com/usmanager/manager/registration-client/reglog"
	"github.com/usmanager/manager/registration-client/util"
)

// heartbeatInterval on timer
const heartbeatInterval = 30 * time.Second

// registerDelay to register on eureka
const registerDelay = 30 * time.Second

// maxHeartbeatsTries max heartbeat retries when not found process
const maxHeartbeatsTries = 5

var instance eureka.Instance
var e eureka.EurekaConnection

var lastApps map[string][]*eureka.Instance
var lastAppsUpdate map[string]time.Time

var execapp *string
var app *string
var eurekaHost *string
var port *string
var hostname *string
var serverPort *string
var serviceContinent *string
var serviceRegion *string
var serviceCountry *string
var serviceCity *string
var autoRegister *string
var monitorExecApp *string
var appEndpointCacheTime *string
var appIsRegistered = false
var appIsUp = true
var sendLocationDataPeriod *string

func main() {
	reglog.Logger.Infof("Started registration-client")

	execapp = flag.String("execapp", "exec-app", "App to monitor")
	app = flag.String("app", "app-name", "App name")
	eurekaHost = flag.String("eureka", "127.0.0.1:8761", "Eureka server")
	port = flag.String("port", "80", "App port")
	hostname = flag.String("hostname", "localhost", "App hostname")
	serverPort = flag.String("serverport", "1906", "Port to bind HTTP listener")
	serviceContinent = flag.String("serviceContinent", os.Getenv("SERVICE_CONTINENT"), "Service location: continent")
	serviceRegion = flag.String("serviceRegion", os.Getenv("SERVICE_REGION"), "Service location: region")
	serviceCountry = flag.String("serviceCountry", os.Getenv("SERVICE_COUNTRY"), "Service location: country")
	serviceCity = flag.String("serviceCity", os.Getenv("SERVICE_CITY"), "Service location: city")

	autoRegister = flag.String("autoregister", "true", "True: registration-client will register app on Eureka; False: app will trigger the register")
	monitorExecApp = flag.String("monitorexecapp", "true", "True: registration-client will monitor exec app; False: no monitoring")
	appEndpointCacheTime = flag.String("appendpointcachetime", "10", "Time to keep an instances endpoints before contacting Eureka")

	sendLocationDataPeriod = flag.String("locationdatatime", "5", "Interval time to send location data")

	flag.Parse()

	//stuff
	errc := make(chan error)
	lastApps = make(map[string][]*eureka.Instance)
	lastAppsUpdate = make(map[string]time.Time)
	go locationutils.InitLocationData(*hostname, *serviceContinent, *serviceRegion, *serviceCountry, *serviceCity, *sendLocationDataPeriod)

	// Create and launch the HTTP server.
	go func() {
		router := mux.NewRouter()
		router.HandleFunc("/api/apps/{appName}", GetAppByName).Methods("GET")
		router.HandleFunc("/api/apps/{appName}/all", GetAllAppsByName).Methods("GET")
		router.HandleFunc("/api/register", RegisterApp).Methods("GET")
		router.HandleFunc("/api/metrics", ReceiveCustomLocationData).Methods("POST")
		reglog.Logger.Infof("Registration-client is listening on port %s. Go to http://127.0.0.1:%s", *serverPort, *serverPort)
		reglog.Logger.Fatal(http.ListenAndServe(":"+*serverPort, router))
	}()

	if strings.EqualFold(*autoRegister, "true") {
		go registerInstance()
	}

	// Capture interrupts.
	go func() {
		c := make(chan os.Signal)
		signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
		errc <- fmt.Errorf("%s", <-c)
	}()
	prepareToTerminateApp(&instance)
}

func registerInstance() {
	if !appIsRegistered {
		e = eureka.NewConn("http://" + *eurekaHost + "/eureka")
		setInstance(strings.ToLower(*app), *hostname, *port, *serviceContinent, *serviceRegion, *serviceCountry, *serviceCity)
		if strings.EqualFold(*autoRegister, "true") {
			time.Sleep(registerDelay)
		}
		err := e.ReregisterInstance(&instance)
		if err == nil {
			appIsRegistered = true
			go heartbeat()
		}
	}
}

func setInstance(app string, hostname string, port string, serviceContinent string,
	serviceRegion string, serviceCountry string, serviceCity string) {
	appPort, _ := strconv.Atoi(port)
	instance = eureka.Instance{
		InstanceId:       app + "_" + hostname + "_" + port,
		HostName:         hostname,
		App:              app,
		IPAddr:           hostname,
		VipAddress:       app,
		SecureVipAddress: app,

		Status: eureka.UP,

		Port:              appPort,
		PortEnabled:       true,
		SecurePort:        appPort,
		SecurePortEnabled: false,

		HomePageUrl:    "http://" + hostname + ":" + port,
		StatusPageUrl:  "http://" + hostname + ":" + port + "/health",
		HealthCheckUrl: "http://" + hostname + ":" + port + "/health",

		CountryId: 1,
		DataCenterInfo: eureka.DataCenterInfo{
			Name: "Amazon",
			Metadata: eureka.AmazonMetadataType{
				InstanceID: app + "_" + hostname + "_" + port,
			},
		},
	}
	instance.SetMetadataString("management.port", port)
	instance.SetMetadataString("serviceContinent", serviceContinent)
	instance.SetMetadataString("serviceRegion", serviceRegion)
	instance.SetMetadataString("serviceCountry", serviceCountry)
	instance.SetMetadataString("serviceCity", serviceCity)
}

func heartbeat() {
	var retries = 0
	var isMonitorOn = strings.EqualFold(*monitorExecApp, "true")
	var heartbeatError = false
	var foundProcess = false
	time.Sleep(heartbeatInterval)
	c := time.Tick(heartbeatInterval)
	for hearthBeatTime := range c {
		if appIsRegistered && appIsUp {
			if isMonitorOn {
				pid, processName, found := util.FindProcess(*execapp)
				foundProcess = found
				reglog.Logger.Infof("PID: %d | Process: %s | Found: %t", pid, processName, found)
			}
			if foundProcess || !isMonitorOn {
				err := e.HeartBeatInstance(&instance)
				if err != nil {
					heartbeatError = true
					reglog.Logger.Errorf("Heartbeat error: %s", err.Error())
				} else {
					retries = 0
					heartbeatError = false
					reglog.Logger.Infof("Heartbeat to eureka server at: %v", hearthBeatTime)
				}
			}
			if (!foundProcess && isMonitorOn) || heartbeatError {
				if retries < maxHeartbeatsTries {
					retries++
					reglog.Logger.Errorf("Heartbeat not sended...")
					err := e.UpdateInstanceStatus(&instance, eureka.DOWN)
					if err != nil {
						reglog.Logger.Errorf("Update instance status error: %s", err.Error())
					}
				} else {
					reglog.Logger.Infof("Max heartbeats retries, deregistering instance...")
					err := e.DeregisterInstance(&instance)
					if err != nil {
						reglog.Logger.Errorf("Deregister instance error: %s", err.Error())
					}
					os.Exit(0)
				}
			}
		}
	}
}

// GetAppByName returns the app endpoint
func GetAppByName(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	emptyRes := api.Empty{}
	vars := mux.Vars(r)
	appName := strings.ToLower(vars["appName"])
	foundApp := false
	_, hasApp := lastApps[appName]

	go locationutils.AddLocationData(appName)

	if hasApp {
		seconds, _ := strconv.Atoi(*appEndpointCacheTime)
		if beforeSomeSeconds(lastAppsUpdate[appName], seconds) {
			foundApp = true
			app := eureka.GetBestInstance(&instance, lastApps[appName])
			reglog.Logger.Infof("Instance chosen for '%s': %s", appName, app.InstanceId)
			w.WriteHeader(http.StatusOK)
			json.NewEncoder(w).Encode(app)
		}
	}
	if !foundApp {
		instances, err := e.GetInstancesByVIPAddress(appName, false, eureka.ThatAreUp)
		if err != nil {
			reglog.Logger.Errorf("Error getting instances from eureka: %s", err.Error())
			w.WriteHeader(http.StatusNotFound)
			json.NewEncoder(w).Encode(emptyRes)
		} else {
			reglog.Logger.Infof("Found %d instances", len(instances))
			if len(instances) == 0 {
				w.WriteHeader(http.StatusNotFound)
				json.NewEncoder(w).Encode(emptyRes)
			} else {
				app := eureka.GetBestInstance(&instance, instances)
				lastApps[appName] = instances
				lastAppsUpdate[appName] = time.Now()
				reglog.Logger.Infof("Instance chosen for '%s': %s", appName, app.InstanceId)
				w.WriteHeader(http.StatusOK)
				json.NewEncoder(w).Encode(app)
			}
		}
	}
}

// GetAllAppsByName returns all apps endpoints
func GetAllAppsByName(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	vars := mux.Vars(r)
	appName := strings.ToLower(vars["appName"])

	instances, err := e.GetInstancesByVIPAddress(appName, false, eureka.InstanceQueryOption(eureka.ThatAreUp))
	if err != nil {
		reglog.Logger.Errorf("Error getting instances from eureka: %s", err.Error())
		w.WriteHeader(http.StatusNotFound)
		json.NewEncoder(w).Encode(make([]string, 0))
	} else {
		reglog.Logger.Infof("Found %d instances", len(instances))
		if len(instances) == 0 {
			w.WriteHeader(http.StatusNotFound)
			json.NewEncoder(w).Encode(make([]string, 0))
		} else {
			apps := eureka.GetApps(instances)
			w.WriteHeader(http.StatusOK)
			json.NewEncoder(w).Encode(apps)
		}
	}
}

// RegisterApp register an app
func RegisterApp(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	msg := api.Msg{
		Content: "success",
	}
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(msg)
	go registerInstance()
	reglog.Logger.Infof("App was registered by request")
}

// ReceiveCustomLocationData receive custom location data
func ReceiveCustomLocationData(w http.ResponseWriter, r *http.Request) {
	var customLocationData api.LocationMonitoringCustom
	_ = json.NewDecoder(r.Body).Decode(&customLocationData)
	go locationutils.AddCustomLocationData(customLocationData.FromContinent, customLocationData.FromCountry, customLocationData.FromCity, customLocationData.ToService)
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	msg := api.Msg{
		Content: "success",
	}
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(msg)
}

func prepareToTerminateApp(instance *eureka.Instance) {
	appIsUp = false
	err := e.UpdateInstanceStatus(instance, eureka.DOWN)
	if err != nil {
		reglog.Logger.Errorf("Update instance status error: %s", err.Error())
	}
	exitApp(instance)
}

func exitApp(instance *eureka.Instance) {
	reglog.Logger.Info("Registration-client is finishing app de-registing, before exit...")
	time.Sleep(20 * time.Second)
	err := e.DeregisterInstance(instance)
	if err != nil {
		reglog.Logger.Errorf("Deregister instance error: %s", err.Error())
	}
	reglog.Logger.Info("Exit registration-client")
	os.Exit(0)
}

func beforeSomeSeconds(timeToCheck time.Time, seconds int) bool {
	timeNow := time.Now()
	timeMax := timeToCheck.Add(time.Second * time.Duration(seconds))
	return timeMax.After(timeNow)
}
