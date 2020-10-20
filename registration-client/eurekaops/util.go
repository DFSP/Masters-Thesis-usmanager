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
	"math/rand"
	"strings"
)

//GetBestInstance returns the best app instance
func GetBestInstance(thisInstance *Instance, instances []*Instance) data.InstanceEndpoint {
	var app data.InstanceEndpoint
	thisContinent, thisRegion, thisCountry, thisCity := getInstanceLocationInfo(*thisInstance)

	var instancesAll []data.InstanceEndpoint
	var instancesContinent []data.InstanceEndpoint
	var instancesRegion []data.InstanceEndpoint
	var instancesCountry []data.InstanceEndpoint
	var instancesCity []data.InstanceEndpoint
	for _, appInstance := range instances {
		app = data.InstanceEndpoint{
			InstanceId: appInstance.InstanceId,
			Endpoint:   appInstance.HomePageUrl,
		}
		instancesAll = append(instancesAll, app)

		appContinent, appRegion, appCountry, appCity := getInstanceLocationInfo(*appInstance)
		if strings.EqualFold(thisContinent, appContinent) {
			instancesContinent = append(instancesContinent, app)
		}
		if strings.EqualFold(thisRegion, appRegion) {
			instancesRegion = append(instancesRegion, app)
		}
		if strings.EqualFold(thisCountry, appCountry) && thisCountry != "" && appCountry != "" {
			instancesCountry = append(instancesCountry, app)
		}
		if strings.EqualFold(thisCity, appCity) && thisCity != "" && appCity != "" {
			instancesCity = append(instancesCity, app)
		}
	}

	if len(instancesCity) > 0 {
		maxIndex := len(instancesCity)
		app = instancesCity[randomIndex(maxIndex)]
	} else if len(instancesCountry) > 0 {
		maxIndex := len(instancesCountry)
		app = instancesCountry[randomIndex(maxIndex)]
	} else if len(instancesRegion) > 0 {
		maxIndex := len(instancesRegion)
		app = instancesRegion[randomIndex(maxIndex)]
	} else if len(instancesContinent) > 0 {
		maxIndex := len(instancesContinent)
		app = instancesContinent[randomIndex(maxIndex)]
	} else {
		maxIndex := len(instancesAll)
		app = instancesAll[randomIndex(maxIndex)]
	}
	return app
}

//Returns serviceContinent, serviceRegion, serviceCountry, serviceCity
func getInstanceLocationInfo(instance Instance) (string, string, string, string) {
	serviceContinent, err := instance.Metadata.GetString("serviceContinent")
	if err != nil {
		serviceContinent = ""
	}
	serviceRegion, err := instance.Metadata.GetString("serviceRegion")
	if err != nil {
		serviceRegion = ""
	}
	serviceCountry, err := instance.Metadata.GetString("serviceCountry")
	if err != nil {
		serviceCountry = ""
	}
	serviceCity, err := instance.Metadata.GetString("serviceCity")
	if err != nil {
		serviceCity = ""
	}
	return serviceContinent, serviceRegion, serviceCountry, serviceCity
}

func randomIndex(max int) int {
	return rand.Intn(max)
}
