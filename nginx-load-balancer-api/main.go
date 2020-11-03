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
	"strings"

	"github.com/gorilla/mux"

	"github.com/usmanager/manager/nginx-load-balancer-api/api"
)

func main() {
	var port = flag.String("port", "1906", "Port to bind HTTP listener")
	flag.Parse()

	router := mux.NewRouter()
	router.HandleFunc("/_/nginx-load-balancer-api/{service}/servers", api.GetServers).Methods("GET")
	router.HandleFunc("/_/nginx-load-balancer-api/{service}/servers", api.AddServers).Methods("POST")
	router.HandleFunc("/_/nginx-load-balancer-api/{service}/servers/{server}", api.DeleteServer).Methods("DELETE")
	log.Printf("Nginx API is listening on port %s", *port)
	log.Fatal(http.ListenAndServe(":"+*port, trimmingMiddleware(router)))
}

func trimmingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		r.URL.Path = strings.TrimSuffix(r.URL.Path, "/")
		next.ServeHTTP(w, r)
	})
}