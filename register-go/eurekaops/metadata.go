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

package eurekaops

// MIT Licensed (see README.md) - Copyright (c) 2013 Hudl <@Hudl>

import (
	"encoding/json"
	"fmt"

	"github.com/usmanager/manager/register-go/reglog"
	"github.com/clbanning/mxj/x2j"
)

// ParseAllMetadata iterates through all instances in an application
func (a *Application) ParseAllMetadata() error {
	for _, instance := range a.Instances {
		err := instance.Metadata.parse()
		if err != nil {
			reglog.Logger.Errorf("Failed parsing metadata for Instance=%s of Application=%s: %s",
				instance.HostName, a.Name, err.Error())
			return err
		}
	}
	return nil
}

// SetMetadataString for a given instance before register
func (ins *Instance) SetMetadataString(key, value string) {
	if ins.Metadata.parsed == nil {
		ins.Metadata.parsed = map[string]interface{}{}
	}
	ins.Metadata.parsed[key] = value
}

func (im *InstanceMetadata) parse() error {
	if len(im.Raw) == 0 {
		im.parsed = make(map[string]interface{})
		return nil
	}

	if len(im.Raw) > 0 && im.Raw[0] == '{' {
		// JSON
		err := json.Unmarshal(im.Raw, &im.parsed)
		if err != nil {
			reglog.Logger.Errorf("Error unmarshalling: %s", err.Error())
			return fmt.Errorf("error unmarshalling: %s\n", err.Error())
		}
	} else {
		// XML: wrap in a BS xml tag so all metadata tags are pulled
		fullDoc := append(append([]byte("<d>"), im.Raw...), []byte("</d>")...)
		parsedDoc, err := x2j.XmlToMap(fullDoc)
		if err != nil {
			reglog.Logger.Errorf("Error unmarshalling: %s", err.Error())
			return fmt.Errorf("error unmarshalling: %s\n", err.Error())
		}
		im.parsed = parsedDoc["d"].(map[string]interface{})
	}
	return nil
}

// GetMap returns a map of the metadata parameters for this instance
func (im *InstanceMetadata) GetMap() map[string]interface{} {
	return im.parsed
}

func (im *InstanceMetadata) getItem(key string) (interface{}, bool, error) {
	err := im.parse()
	if err != nil {
		return "", false, fmt.Errorf("parsing error: %s\n", err.Error())
	}
	val, present := im.parsed[key]
	return val, present, nil
}

// GetString pulls a value cast as a string. Swallows panics from type
// assertion and returns empty string + an error if conversion fails
func (im *InstanceMetadata) GetString(key string) (s string, err error) {
	defer func() {
		if r := recover(); r != nil {
			s = ""
			err = fmt.Errorf("failed to cast interface to string\n")
		}
	}()
	v, prs, err := im.getItem(key)
	if !prs {
		return "", err
	}
	return v.(string), err
}

// GetInt pulls a value cast as int. Swallows panics from type assertion and
// returns 0 + an error if conversion fails
func (im *InstanceMetadata) GetInt(key string) (i int, err error) {
	defer func() {
		if r := recover(); r != nil {
			i = 0
			err = fmt.Errorf("failed to cast interface to int\n")
		}
	}()
	v, err := im.GetFloat64(key)
	return int(v), err
}

// GetFloat32 pulls a value cast as float. Swallows panics from type assertion
// and returns 0.0 + an error if conversion fails
func (im *InstanceMetadata) GetFloat32(key string) (f float32, err error) {
	defer func() {
		if r := recover(); r != nil {
			f = 0.0
			err = fmt.Errorf("failed to cast interface to float32\n")
		}
	}()
	v, err := im.GetFloat64(key)
	return float32(v), err
}

// GetFloat64 pulls a value cast as float. Swallows panics from type assertion
// and returns 0.0 + an error if conversion fails
func (im *InstanceMetadata) GetFloat64(key string) (f float64, err error) {
	defer func() {
		if r := recover(); r != nil {
			f = 0.0
			err = fmt.Errorf("failed to cast interface to float64\n")
		}
	}()
	v, prs, err := im.getItem(key)
	if !prs {
		return 0.0, err
	}
	return v.(float64), err
}

// GetBool pulls a value cast as bool.  Swallows panics from type assertion and
// returns false + an error if conversion fails
func (im *InstanceMetadata) GetBool(key string) (b bool, err error) {
	defer func() {
		if r := recover(); r != nil {
			b = false
			err = fmt.Errorf("failed to cast interface to bool\n")
		}
	}()
	v, prs, err := im.getItem(key)
	if !prs {
		return false, err
	}
	return v.(bool), err
}
