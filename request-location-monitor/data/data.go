/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package data

import (
	"time"

	"github.com/usmanager/manager/request-location-monitor/utils"
)

// LocationMonitoringData data
var LocationMonitoringData *utils.ConcurrentMap

// LocationMonitoringSimple type
type LocationMonitoringSimple struct {
	FromContinent string `json:"fromContinent"`
	FromRegion    string `json:"fromRegion"`
	FromCountry   string `json:"fromCountry"`
	FromCity      string `json:"fromCity"`
	ToService     string `json:"toService"`
	Count         int    `json:"count"`
}

// LocationMonitoring type
type LocationMonitoring struct {
	FromContinent string    `json:"fromContinent"`
	FromRegion    string    `json:"fromRegion"`
	FromCountry   string    `json:"fromCountry"`
	FromCity      string    `json:"fromCity"`
	ToService     string    `json:"toService"`
	Count         int       `json:"count"`
	Timestamp     time.Time `json:"timestamp"`
}

// LocationMonitoringResp type
type LocationMonitoringResp struct {
	ToService    string                   `json:"toService"`
	LocationData LocationMonitoringSimple `json:"locationData"`
}

// LocationMonitoringRespAll type
type LocationMonitoringRespAll struct {
	ToService    string             `json:"toService"`
	LocationData LocationMonitoring `json:"locationData"`
}

// Message type
type Message struct {
	Message string `json:"message"`
}

func init() {
	LocationMonitoringData = utils.NewConcurrentMap()
}
