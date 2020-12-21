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
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/usmanager/manager/registration-client/reglog"
)

var register bool
var interval int
var registerPort int

func init() {
	flag.BoolVar(&register, "register", true, "True: registration-client will register service on Eureka; False: service will manually trigger the register")
	flag.IntVar(&interval, "interval", 5000, "Interval time (in ms) to send location data")
	flag.IntVar(&registerPort, "register-port", 1906, "Port to start the http server")
}

func main() {
	flag.Parse()

	address := fmt.Sprintf(":%d", registerPort)
	listen, err := net.Listen("tcp", address)
	if err != nil {
		reglog.Logger.Fatal(err)
	}
	reglog.Logger.Infof("Registration-client is listening on port %d", registerPort)

	if register {
		go func() {
			for i := 0; i < 5; i++ {
				err := instance.Register()
				if err == nil {
					break
				}
				reglog.Logger.Error(err)
				time.Sleep(5 * time.Second)
			}
		}()
	}

	go func() {
		router := mux.NewRouter()
		router.HandleFunc("/api/register", api.RegisterServiceEndpoint).Methods("POST")
		router.HandleFunc("/api/services/{service}/endpoint", api.GetServiceEndpoint).Methods("GET")
		router.Methods("GET").
			Path("/api/services/{service}/endpoint").
			Queries("among", "x").
			HandlerFunc(api.GetServiceEndpoint)
		router.Methods("GET").
			Path("/api/services/{service}/endpoint").
			Queries("distance", "d").
			HandlerFunc(api.GetServiceEndpoint)
		router.HandleFunc("/api/services/{service}/endpoints", api.GetServiceEndpoints).Methods("GET")
		router.HandleFunc("/api/metrics", api.RegisterLocationMonitoring).Methods("POST")
		reglog.Logger.Fatal(http.Serve(listen, trimmingMiddleware(router)))
	}()

	location.SendLocationTimer(time.Duration(interval) * time.Millisecond)

	signalChan := make(chan os.Signal, 1)

	signal.Notify(
		signalChan,
		syscall.SIGHUP,  // kill -SIGHUP XXXX
		syscall.SIGINT,  // kill -SIGINT XXXX or Ctrl+c
		syscall.SIGQUIT, // kill -SIGQUIT XXXX
	)
	<-signalChan
	log.Print("os.Interrupt - Shutting down...\n")

	// terminate after second signal before callback is done
	go func() {
		<-signalChan
		log.Fatal("os.Kill - Terminating...\n")
	}()

	instance.Deregister()
}

func trimmingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		r.URL.Path = strings.TrimSuffix(r.URL.Path, "/")
		next.ServeHTTP(w, r)
	})
}
