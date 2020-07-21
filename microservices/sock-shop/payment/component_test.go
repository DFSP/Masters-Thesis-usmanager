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
	"bytes"
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"testing"

	"golang.org/x/net/context"
)

func TestComponent(t *testing.T) {
	// Mechanical stuff.
	ctx := context.Background()

	handler, logger := WireUp(ctx, float32(99.99), opentracing.GlobalTracer(), "test")

	ts := httptest.NewServer(handler)
	defer ts.Close()

	var request AuthoriseRequest
	request.Amount = 9.99
	requestBytes, err := json.Marshal(request)
	if err != nil {
		t.Fatal("ERROR", err)
	}

	res, err := http.Post(ts.URL+"/paymentAuth", "application/json", bytes.NewReader(requestBytes))
	if err != nil {
		t.Fatal("ERROR", err)
	}
	greeting, err := ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		t.Fatal("ERROR", err)
	}
	var response Authorisation
	json.Unmarshal(greeting, &response)

	logger.Log("Authorised", response.Authorised)

	expected := true
	if response.Authorised != expected {
		t.Errorf("Authorise returned unexpected result: got %v expected %v",
			response.Authorised, expected)
	}

}
