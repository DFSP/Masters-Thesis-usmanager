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
	"github.com/usmanager/manager/registration-client/util"
	"os"
	"time"
)

const heartbeatInterval = 30 * time.Second
const maxHeartbeatTries = 5

var process string

func init() {
	flag.StringVar(&process, "process", "", "Process name to monitor")
}

func Heartbeat(eurekaServer eureka.EurekaConnection, instance eureka.Instance) {
	time.Sleep(heartbeatInterval)

	var retries = 0
	var heartbeatError = false
	var foundProcess = false

	heartbeatTicker := time.NewTicker(heartbeatInterval)
	defer heartbeatTicker.Stop()

	var monitoringProcess = len(process) > 0
	go func() {
		for {
			select {
			case t := <-heartbeatTicker.C:
				if monitoringProcess {
					pid, processName, found := util.FindProcess(process)
					foundProcess = found
					reglog.Logger.Infof("PID: %d | Process: %s | Found: %t", pid, processName, found)
				}
				if foundProcess || !monitoringProcess {
					err := eurekaServer.HeartBeatInstance(&instance)
					if err != nil {
						heartbeatError = true
						reglog.Logger.Errorf("Heartbeat error: %s", err.Error())
					} else {
						retries = 0
						heartbeatError = false
						reglog.Logger.Infof("Heartbeat to eureka server at: %v", t)
					}
				}
				if (!foundProcess && monitoringProcess) || heartbeatError {
					if retries < maxHeartbeatTries {
						retries++
						reglog.Logger.Errorf("Heartbeat not sent, retry #%s...", retries)
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
			}
		}
	}()

}