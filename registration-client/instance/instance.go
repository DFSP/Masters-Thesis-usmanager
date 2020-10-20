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

package instance

import (
	"flag"
	"fmt"
	eureka "github.com/usmanager/manager/registration-client/eurekaops"
	"github.com/usmanager/manager/registration-client/heartbeat"
	"github.com/usmanager/manager/registration-client/reglog"
	"os"
	"strconv"
	"strings"
)

var Service string
var Hostname string
var Port int
var Latitude float64
var Longitude float64

var EurekaServer eureka.EurekaConnection
var RequestLocationMonitorUrl string
var Instance eureka.Instance

func init() {
	flag.StringVar(&Service, "service", "", "Service name")
	Service = strings.ToLower(Service)
	flag.StringVar(&Hostname, "hostname", "localhost", "Service Hostname")
	flag.IntVar(&Port, "port", 80, "Service Port")
	lat, _ := strconv.ParseFloat(os.Getenv("Latitude"), 64)
	flag.Float64Var(&Latitude, "latitude", lat, "Service Latitude")
	lon, _ := strconv.ParseFloat(os.Getenv("Longitude"), 64)
	flag.Float64Var(&Longitude, "longitude", lon, "Service Longitude")
	eurekaAddress := flag.String("eureka-server", "127.0.0.1:8761", "Eureka server")

	eurekaUrl := fmt.Sprintf("http://%s/eureka", *eurekaAddress)
	EurekaServer = eureka.NewConn(eurekaUrl)

	RequestLocationMonitorUrl = fmt.Sprintf("http://%s:%d/api/monitoring", Hostname, 1919)
}

func Register() {
	Instance = eureka.Instance{
		InstanceId:       fmt.Sprintf("%s_%s_%d", Service, Hostname, Port),
		HostName:         Hostname,
		App:              Service,
		IPAddr:           Hostname,
		VipAddress:       Service,
		SecureVipAddress: Service,

		Status: eureka.UP,

		Port:              Port,
		PortEnabled:       true,
		SecurePort:        Port,
		SecurePortEnabled: false,

		HomePageUrl:    fmt.Sprintf("http://%s:%d", Hostname, Port),
		StatusPageUrl:  fmt.Sprintf("http://%s:%d/health", Hostname, Port),
		HealthCheckUrl: fmt.Sprintf("http://%s:%d/health", Hostname, Port),
	}
	Instance.SetMetadataString("management.Port", strconv.Itoa(Port))
	Instance.SetMetadataString("Latitude", strconv.FormatFloat(Latitude, 'f', -1, 64))
	Instance.SetMetadataString("Longitude", strconv.FormatFloat(Longitude, 'f', -1, 64))

	err := EurekaServer.ReregisterInstance(&Instance)
	if err == nil {
		go heartbeat.Heartbeat(EurekaServer, Instance)
	}

	reglog.Logger.Infof("Instance registered as %s", Instance.InstanceId)
}

func Deregister() {
	err := EurekaServer.UpdateInstanceStatus(&Instance, eureka.DOWN)
	if err != nil {
		reglog.Logger.Errorf("Update instance status error: %s", err.Error())
	}
	reglog.Logger.Info("Registration-client is finishing app de-registering before exit")
	// TODO wtf
	/*time.Sleep(20 * time.Second)*/
	err = EurekaServer.DeregisterInstance(&Instance)
	if err != nil {
		reglog.Logger.Errorf("Deregister instance error: %s", err.Error())
	}
	reglog.Logger.Info("Exit registration-client")
	os.Exit(0)
}
