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

package payment

import (
	"net/http"
	"os"

	"github.com/go-kit/kit/log"
	"golang.org/x/net/context"

	stdopentracing "github.com/opentracing/opentracing-go"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/weaveworks/common/middleware"
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

func WireUp(ctx context.Context, declineAmount float32, tracer stdopentracing.Tracer, serviceName string) (http.Handler, log.Logger) {
	// Log domain.
	var logger log.Logger
	{
		logger = log.NewLogfmtLogger(os.Stderr)
		logger = log.NewContext(logger).With("ts", log.DefaultTimestampUTC)
		logger = log.NewContext(logger).With("caller", log.DefaultCaller)
	}

	// Service domain.
	var service Service
	{
		service = NewAuthorisationService(declineAmount)
		service = LoggingMiddleware(logger)(service)
	}

	// Endpoint domain.
	endpoints := MakeEndpoints(service, tracer)

	router := MakeHTTPHandler(ctx, endpoints, logger, tracer)

	httpMiddleware := []middleware.Interface{
		middleware.Instrument{
			Duration:     HTTPLatency,
			RouteMatcher: router,
		},
	}

	// Handler
	handler := middleware.Merge(httpMiddleware...).Wrap(router)

	return handler, logger
}
