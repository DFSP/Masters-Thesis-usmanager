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
	"github.com/usmanager/manager/request-location-monitor/reglog"
	"log"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"

	"github.com/usmanager/manager/request-location-monitor/api"
)

var port int

func init()  {
	flag.IntVar(&port, "port", 1919, "Port to bind HTTP listener")
}

func main() {
	flag.Parse()

	router := mux.NewRouter()

	router.Methods("GET").
		Path("/api/location/requests").
		HandlerFunc(api.ListLocationRequests)

	router.Methods("GET").
		Path("/api/location/requests").
		Queries("aggregation", "").
		HandlerFunc(api.ListLocationRequests)

	router.Methods("GET").
		Path("/api/location").
		Queries(
			"interval", "",
			"interval", "{[0-9]+}").
		HandlerFunc(api.ListLocationRequests)

	router.Methods("POST").
		Path("/api/location/requests").
		HandlerFunc(api.AddLocationRequest)

	reglog.Logger.Infof("Request-location-monitor is listening at http://127.0.0.1:%d", port)
	log.Fatal(http.ListenAndServe(":"+strconv.Itoa(port), router))
}
