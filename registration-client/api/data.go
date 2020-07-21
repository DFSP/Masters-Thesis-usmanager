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

package api

// App type
type App struct {
	InstanceId string `json:"instanceId,omitempty"`
	Endpoint   string `json:"endpoint,omitempty"`
}

//Empty type
type Empty struct {
}

// LocationMonitoring type
type LocationMonitoring struct {
	Valid         bool   `json:"valid,omitempty"`
	FromContinent string `json:"fromContinent,omitempty"`
	FromRegion    string `json:"fromRegion,omitempty"`
	FromCountry   string `json:"fromCountry,omitempty"`
	FromCity      string `json:"fromCity,omitempty"`
	ToService     string `json:"toService,omitempty"`
	Count         int    `json:"count,omitempty"`
}

// LocationMonitoringSimple type
type LocationMonitoringSimple struct {
	FromContinent string `json:"fromContinent,omitempty"`
	FromRegion    string `json:"fromRegion,omitempty"`
	FromCountry   string `json:"fromCountry,omitempty"`
	FromCity      string `json:"fromCity,omitempty"`
	ToService     string `json:"toService,omitempty"`
	Count         int    `json:"count,omitempty"`
}

// LocationMonitoringCustom type
type LocationMonitoringCustom struct {
	FromContinent string `json:"fromContinent,omitempty"`
	FromCountry   string `json:"fromCountry,omitempty"`
	FromCity      string `json:"fromCity,omitempty"`
	ToService     string `json:"toService,omitempty"`
	Count         int    `json:"count,omitempty"`
}

//Msg type
type Msg struct {
	Content string `json:"content,omitempty"`
}
