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

package heartbeat

import (
	"flag"
	eureka "github.com/usmanager/manager/registration-client/eurekaops"
	"github.com/usmanager/manager/registration-client/reglog"
	ps "github.com/usmanager/manager/registration-client/util"
	"os"
	"time"
)

const heartbeatInterval = 30 * time.Second
const maxHeartbeatTries = 5

var process string

func init() {
	flag.StringVar(&process, "process", "", "Process name to monitor")
}

func Ticker(eurekaServer eureka.EurekaConnection, instance eureka.Instance) chan bool {
	heartbeatTicker := time.NewTicker(heartbeatInterval)

	stopChan := make(chan bool)
	go func(ticker *time.Ticker) {
		defer heartbeatTicker.Stop()

		var retry int

		for {
			select {
			case t := <-ticker.C:
				if heartbeat(eurekaServer, instance, t, retry) {
					retry = 0
				} else {
					retry++
				}
			case stop := <-stopChan:
				if stop {
					return
				}
			}
		}

	}(heartbeatTicker)

	return stopChan
}


func heartbeat(eurekaServer eureka.EurekaConnection, instance eureka.Instance, t time.Time, retry int) bool {
	var success bool
	var foundProcess bool

	var monitoringProcess = len(process) > 0
	if monitoringProcess {
		pid, processName, found := ps.FindProcess(process)
		foundProcess = found
		reglog.Logger.Infof("PID: %d | Process: %s | Found: %t", pid, processName, found)
	}
	if foundProcess || !monitoringProcess {
		err := eurekaServer.HeartBeatInstance(&instance)
		if err != nil {
			reglog.Logger.Errorf("Heartbeat error: %s", err.Error())
		} else {
			reglog.Logger.Infof("Heartbeat to eureka server at: %v", t)
			success = true
		}
	}
	if !success {
		if retry < maxHeartbeatTries {
			reglog.Logger.Errorf("Heartbeat not sent, retry #%d...", retry)
			err := eurekaServer.UpdateInstanceStatus(&instance, eureka.DOWN)
			if err != nil {
				reglog.Logger.Errorf("Update instance status error: %s", err.Error())
			}
		} else {
			reglog.Logger.Infof("Max heartbeat retries, de-registering instance...")
			err := eurekaServer.DeregisterInstance(&instance)
			if err != nil {
				reglog.Logger.Errorf("Deregister instance error: %s", err.Error())
			}
			os.Exit(0)
		}
	}

	return success
}