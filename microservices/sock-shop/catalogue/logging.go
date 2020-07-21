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

package catalogue

import (
	"strings"
	"time"

	"github.com/go-kit/kit/log"
)

// LoggingMiddleware logs method calls, parameters, results, and elapsed time.
func LoggingMiddleware(logger log.Logger) Middleware {
	return func(next Service) Service {
		return loggingMiddleware{
			next:   next,
			logger: logger,
		}
	}
}

type loggingMiddleware struct {
	next   Service
	logger log.Logger
}

func (mw loggingMiddleware) List(tags []string, order string, pageNum, pageSize int) (socks []Sock, err error) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "List",
			"tags", strings.Join(tags, ", "),
			"order", order,
			"pageNum", pageNum,
			"pageSize", pageSize,
			"result", len(socks),
			"err", err,
			"took", time.Since(begin),
		)
	}(time.Now())
	return mw.next.List(tags, order, pageNum, pageSize)
}

func (mw loggingMiddleware) Count(tags []string) (n int, err error) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "Count",
			"tags", strings.Join(tags, ", "),
			"result", n,
			"err", err,
			"took", time.Since(begin),
		)
	}(time.Now())
	return mw.next.Count(tags)
}

func (mw loggingMiddleware) Get(id string) (s Sock, err error) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "Get",
			"id", id,
			"sock", s.ID,
			"err", err,
			"took", time.Since(begin),
		)
	}(time.Now())
	return mw.next.Get(id)
}

func (mw loggingMiddleware) Tags() (tags []string, err error) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "Tags",
			"result", len(tags),
			"err", err,
			"took", time.Since(begin),
		)
	}(time.Now())
	return mw.next.Tags()
}

func (mw loggingMiddleware) Health() (health []Health) {
	defer func(begin time.Time) {
		mw.logger.Log(
			"method", "Health",
			"result", len(health),
			"took", time.Since(begin),
		)
	}(time.Now())
	return mw.next.Health()
}
