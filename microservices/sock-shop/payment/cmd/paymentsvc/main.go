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
	"net"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/go-kit/kit/log"
	stdopentracing "github.com/opentracing/opentracing-go"
	zipkin "github.com/openzipkin/zipkin-go-opentracing"
	"github.com/usmanager/manager/microservices/sock-shop/payment"
	"github.com/usmanager/manager/registration-client-go"
	"golang.org/x/net/context"
)

const (
	//ServiceName payment
	ServiceName = "payment"
)

func main() {
	var (
		port          = flag.String("port", "8080", "Port to bind HTTP listener")
		zip           = flag.String("zipkin", os.Getenv("ZIPKIN"), "Zipkin address")
		declineAmount = flag.Float64("decline", 105, "Decline payments over certain amount")
	)
	flag.Parse()
	var tracer stdopentracing.Tracer
	{
		// Log domain.
		var logger log.Logger
		{
			logger = log.NewLogfmtLogger(os.Stderr)
			logger = log.NewContext(logger).With("ts", log.DefaultTimestampUTC)
			logger = log.NewContext(logger).With("caller", log.DefaultCaller)
		}
		// Find service local IP.
		conn, err := net.Dial("udp", "8.8.8.8:80")
		if err != nil {
			logger.Log("err", err)
			os.Exit(1)
		}
		localAddr := conn.LocalAddr().(*net.UDPAddr)
		host := strings.Split(localAddr.String(), ":")[0]
		defer conn.Close()
		if *zip == "" {
			tracer = stdopentracing.NoopTracer{}
		} else {
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
	// Mechanical stuff.
	errc := make(chan error)
	ctx := context.Background()

	handler, logger := payment.WireUp(ctx, float32(*declineAmount), tracer, ServiceName)

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
