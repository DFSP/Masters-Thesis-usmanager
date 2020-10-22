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
	hash "github.com/usmanager/manager/registration-client/util"
	"strconv"
	"strings"
)

var Service string
var Hostname string
var Port int
var Latitude float64
var Longitude float64

var eurekaAddress string
var EurekaServer eureka.EurekaConnection
var RequestLocationMonitorUrl string
var Instance eureka.Instance

var StopHeartbeatChan chan bool

func init() {
	flag.StringVar(&Service, "service", "", "Service name")
	flag.StringVar(&Hostname, "hostname", "127.0.0.1", "Service Hostname")
	flag.IntVar(&Port, "port", 1906, "Service Port")
	flag.Float64Var(&Latitude, "latitude", 0, "Service Latitude")
	flag.Float64Var(&Longitude, "longitude", 0, "Service Longitude")
	flag.StringVar(&eurekaAddress, "eureka", "127.0.0.1:8761", "Eureka server")
}

func Register() {
	Service = strings.ToLower(Service)
	eurekaUrl := fmt.Sprintf("http://%s/eureka", eurekaAddress)
	EurekaServer = eureka.NewConn(eurekaUrl)

	RequestLocationMonitorUrl = fmt.Sprintf("http://%s:%d/api/monitoring", Hostname, 1919)

	id := fmt.Sprintf("%s_%s_%d", Service, Hostname, Port)
	instanceId := hash.Sha1(id)

	Instance = eureka.Instance{
		InstanceId:       instanceId,
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

		CountryId: 1,
		DataCenterInfo: eureka.DataCenterInfo{
			Name: "Amazon",
			Metadata: eureka.AmazonMetadataType{
				InstanceID: instanceId,
			},
		},
	}
	Instance.SetMetadataString("management.port", strconv.Itoa(Port))
	Instance.SetMetadataString("latitude", strconv.FormatFloat(Latitude, 'f', -1, 64))
	Instance.SetMetadataString("longitude", strconv.FormatFloat(Longitude, 'f', -1, 64))

	err := EurekaServer.ReregisterInstance(&Instance)
	if err == nil {
		reglog.Logger.Infof("Instance registered as %s", Instance.InstanceId)
		StopHeartbeatChan = heartbeat.Ticker(EurekaServer, Instance)
	}

}

func Deregister() {
	err := EurekaServer.UpdateInstanceStatus(&Instance, eureka.DOWN)
	if err != nil {
		reglog.Logger.Errorf("Update instance status error: %s", err.Error())
	}

	err = EurekaServer.DeregisterInstance(&Instance)
	if err != nil {
		reglog.Logger.Errorf("Deregister instance error: %s", err.Error())
	}
}
