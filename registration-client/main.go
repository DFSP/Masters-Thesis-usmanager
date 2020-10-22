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

package main

import (
	"flag"
	"fmt"
	"github.com/gorilla/mux"
	"github.com/usmanager/manager/registration-client/api"
	"github.com/usmanager/manager/registration-client/instance"
	"github.com/usmanager/manager/registration-client/location"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/usmanager/manager/registration-client/reglog"
)

var register bool
var interval int

func init()  {
	flag.BoolVar(&register, "register", true, "True: registration-client will register service on Eureka; False: service will manually trigger the register")
	flag.IntVar(&interval, "interval", 5000, "Interval time (in ms) to send location data")
}

func main() {
	flag.Parse()

	address := fmt.Sprintf(":%d", instance.Port)
	listen, err := net.Listen("tcp", address)
	if err != nil {
		reglog.Logger.Fatal(err)
	}
	reglog.Logger.Infof("Registration-client is listening on port %d", instance.Port)

	if register {
		go func() {
			instance.Register()
		}()
	}

	go func() {
		router := mux.NewRouter()
		router.HandleFunc("/api/register", api.RegisterServiceEndpoint).Methods("POST")
		router.HandleFunc("/api/services/{service}/endpoint", api.GetServiceEndpoint).Methods("GET")
		router.HandleFunc("/api/services/{service}/endpoints", api.GetServiceEndpoints).Methods("GET")
		router.HandleFunc("/api/metrics", api.RegisterLocationMonitoring).Methods("POST")
		reglog.Logger.Fatal(http.Serve(listen, router))
	}()

	sendLocationTimerStopChan := location.SendLocationTimer(time.Duration(interval) * time.Millisecond)

	interrupt := make(chan error)
	go func() {
		c := make(chan os.Signal)
		signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
		interrupt <- fmt.Errorf("%s", <-c)
	}()
	<-interrupt

	instance.StopHeartbeatChan <- true
	close(instance.StopHeartbeatChan)

	sendLocationTimerStopChan <- true
	close(sendLocationTimerStopChan)

	instance.Deregister()
}
