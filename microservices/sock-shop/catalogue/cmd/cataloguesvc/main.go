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
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/go-kit/kit/log"
	stdopentracing "github.com/opentracing/opentracing-go"
	zipkin "github.com/openzipkin/zipkin-go-opentracing"

	"net"
	"net/http"

	"path/filepath"

	"github.com/usmanager/manager/microservices/sock-shop/catalogue"
	"github.com/usmanager/manager/registration-client-go"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/weaveworks/common/middleware"
	"golang.org/x/net/context"
)

const (
	//ServiceName catalogue
	ServiceName = "catalogue"
)

var (
	HTTPLatency = prometheus.NewHistogramVec(prometheus.HistogramOpts{
		Name:    "request_duration_seconds",
		Help:    "Time (in seconds) spent serving HTTP requests.",
		Buckets: prometheus.DefBuckets,
	}, []string{"method", "route", "status_code", "isWS"})
)

func init() {
	prometheus.MustRegister(HTTPLatency)
}

func main() {
	var (
		port   = flag.String("port", "8081", "Port to bind HTTP listener") // TODO(pb): should be -addr, default ":8081"
		images = flag.String("images", "./images/", "Image path")
		dsn    = flag.String("DSN", "catalogue-db:3306", "Data Source Name: hostname:port")
		zip    = flag.String("zipkin", os.Getenv("ZIPKIN"), "Zipkin address")
	)
	flag.Parse()
	dsnFinal := "catalogue_user:default_password@tcp(" + *dsn + ")/socksdb"

	fmt.Fprintf(os.Stderr, "images: %q\n", *images)
	abs, err := filepath.Abs(*images)
	fmt.Fprintf(os.Stderr, "Abs(images): %q (%v)\n", abs, err)
	pwd, err := os.Getwd()
	fmt.Fprintf(os.Stderr, "Getwd: %q (%v)\n", pwd, err)
	files, _ := filepath.Glob(*images + "/*")
	fmt.Fprintf(os.Stderr, "ls: %q\n", files) // contains a list of all files in the current directory

	// Mechanical stuff.
	errc := make(chan error)
	ctx := context.Background()

	// Log domain.
	var logger log.Logger
	{
		logger = log.NewLogfmtLogger(os.Stderr)
		logger = log.NewContext(logger).With("ts", log.DefaultTimestampUTC)
		logger = log.NewContext(logger).With("caller", log.DefaultCaller)
	}

	var tracer stdopentracing.Tracer
	{
		if *zip == "" {
			tracer = stdopentracing.NoopTracer{}
		} else {
			// Find service local IP.
			conn, err := net.Dial("udp", "8.8.8.8:80")
			if err != nil {
				logger.Log("err", err)
				os.Exit(1)
			}
			localAddr := conn.LocalAddr().(*net.UDPAddr)
			host := strings.Split(localAddr.String(), ":")[0]
			defer conn.Close()
			logger := log.NewContext(logger).With("tracer", "Zipkin")
			logger.Log("addr", zip)
			collector, err := zipkin.NewHTTPCollector(
				*zip,
				zipkin.HTTPLogger(logger),
			)
			if err != nil {
				logger.Log("err", err)
				os.Exit(1)
			}
			tracer, err = zipkin.NewTracer(
				zipkin.NewRecorder(collector, false, fmt.Sprintf("%v:%v", host, port), ServiceName),
			)
			if err != nil {
				logger.Log("err", err)
				os.Exit(1)
			}
		}
		stdopentracing.InitGlobalTracer(tracer)
	}

	// Data domain.
	var db *sqlx.DB
	var errDB error

	for index := 0; index < 5; index++ {
		db, errDB = sqlx.Open("mysql", dsnFinal)
		if errDB != nil {
			logger.Log("err", errDB)
		} else {
			break
		}
		time.Sleep(5 * time.Second)
	}
	if errDB != nil {
		os.Exit(1)
	}
	defer db.Close()

	// Check if DB connection can be made, only for logging purposes, should not fail/exit
	for index := 0; index < 5; index++ {
		errDB = db.Ping()
		if errDB != nil {
			logger.Log("Error", "Unable to connect to Database", "DSN", dsnFinal)
		} else {
			logger.Log("Database is on")
			break
		}
		time.Sleep(5 * time.Second)
	}

	// Service domain.
	var service catalogue.Service
	{
		service = catalogue.NewCatalogueService(db, logger)
		service = catalogue.LoggingMiddleware(logger)(service)
	}

	// Endpoint domain.
	endpoints := catalogue.MakeEndpoints(service, tracer)

	// HTTP router
	router := catalogue.MakeHTTPHandler(ctx, endpoints, *images, logger, tracer)

	httpMiddleware := []middleware.Interface{
		middleware.Instrument{
			Duration:     HTTPLatency,
			RouteMatcher: router,
		},
	}

	// Handler
	handler := middleware.Merge(httpMiddleware...).Wrap(router)

	// Create and launch the HTTP server.
	go func() {
		logger.Log("transport", "HTTP", "port", *port)
		errc <- http.ListenAndServe(":"+*port, handler)
	}()

	apiClient := registration.NewAPIClient(registration.NewConfiguration())

	for index := 0; index < 5; index++ {
		_, err := apiClient.EndpointsApi.Register(ctx)
		if err != nil {
			logger.Log("Error", "Fail to register app", "err", err)
		} else {
			break
		}
		time.Sleep(5 * time.Second)
	}

	// Capture interrupts.
	go func() {
		c := make(chan os.Signal)
		signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
		errc <- fmt.Errorf("%s", <-c)
	}()

	logger.Log("exit", <-errc)
}
