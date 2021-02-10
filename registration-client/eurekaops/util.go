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

import (
	"github.com/usmanager/manager/registration-client/data"
	"math"
	"sort"
	"strconv"
)

func distanceBetween(oneInstance *Instance, anotherInstance *Instance) float64 {
	oneLat, _ := oneInstance.Metadata.GetString("latitude")
	oneLatitude, _ := strconv.ParseFloat(oneLat, 64)
	oneLon, _ := oneInstance.Metadata.GetString("longitude")
	oneLongitude, _ := strconv.ParseFloat(oneLon, 64)

	anotherLat, _ := anotherInstance.Metadata.GetString("latitude")
	anotherLatitude, _ := strconv.ParseFloat(anotherLat, 64)
	anotherLon, _ := anotherInstance.Metadata.GetString("longitude")
	anotherLongitude, _ := strconv.ParseFloat(anotherLon, 64)

	ph1 := oneLatitude * math.Pi / 180
	ph2 := anotherLatitude * math.Pi / 180
	deltaY := (anotherLongitude - oneLongitude) * math.Pi / 180
	r := 6371e3

	return math.Acos(math.Sin(ph1)*math.Sin(ph2)+math.Cos(ph1)*math.Cos(ph2)*math.Cos(deltaY)) * r
}

func GetBestInstance(thisInstance *Instance, instances []*Instance) data.InstanceEndpoint {
	// sort instances by distance
	sort.Slice(instances, func(i, j int) bool {
		distanceToOne := distanceBetween(thisInstance, instances[i])
		distanceToAnother := distanceBetween(thisInstance, instances[j])
		return distanceToOne < distanceToAnother
	})

	// ?number=x&range=d
	// always choose closest
	// randomize between the x closest
	// randomize between the instances within a distance range? 100 km
	// TODO

	instance := instances[0]
	return data.InstanceEndpoint{
		InstanceId: instance.InstanceId,
		Endpoint:   instance.HomePageUrl,
	}
}
