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

package eurekaops

// MIT Licensed (see README.md) - Copyright (c) 2013 Hudl <@Hudl>

import (
	"fmt"
)

type unsuccessfulHTTPResponse struct {
	statusCode    int
	messagePrefix string
}

func (u *unsuccessfulHTTPResponse) Error() string {
	if len(u.messagePrefix) > 0 {
		return fmt.Sprint(u.messagePrefix, ", rcode = ", u.statusCode)
	}
	return fmt.Sprint("rcode = ", u.statusCode)
}

// HTTPResponseStatusCode extracts the HTTP status code for the response from Eureka that motivated
// the supplied error, if any. If the returned present value is true, the returned code is an HTTP
// status code.
func HTTPResponseStatusCode(err error) (code int, present bool) {
	if u, ok := err.(*unsuccessfulHTTPResponse); ok {
		return u.statusCode, true
	}
	return 0, false
}

type AppNotFoundError struct {
	specific string
}

func (e AppNotFoundError) Error() string {
	return "Application not found for name=" + e.specific
}
