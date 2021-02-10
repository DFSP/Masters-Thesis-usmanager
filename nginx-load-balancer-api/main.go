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
	"log"
	"net/http"
	"strconv"
	"strings"

	"github.com/gorilla/mux"

	"github.com/usmanager/manager/nginx-load-balancer-api/api"
)

func main() {
	var port = flag.Int("port", 1906, "Port to bind HTTP listener")
	flag.Parse()

	router := mux.NewRouter()
	router.HandleFunc("/api/servers", api.GetServers).Methods("GET")
	router.HandleFunc("/api/{service}/servers", api.GetServiceServers).Methods("GET")
	router.HandleFunc("/api/{service}/servers", api.AddServiceServers).Methods("POST")
	router.HandleFunc("/api/{service}/servers/{server}", api.DeleteServiceServer).Methods("DELETE")
	log.Printf("Nginx API is listening on port %d", *port)
	log.Fatal(http.ListenAndServe(":"+strconv.Itoa(*port), trimmingMiddleware(router)))
}

func trimmingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		r.URL.Path = strings.TrimSuffix(r.URL.Path, "/")
		next.ServeHTTP(w, r)
	})
}
