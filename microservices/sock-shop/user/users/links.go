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

package users

import (
	"flag"
	"fmt"
	"os"
)

var (
	domain    string
	entitymap = map[string]string{
		"customer": "customers",
		"address":  "addresses",
		"card":     "cards",
	}
)

func init() {
	flag.StringVar(&domain, "link-domain", os.Getenv("HATEAOS"), "HATEAOS link domain")
}

type Links map[string]Href

func (l *Links) AddLink(ent string, id string) {
	nl := make(Links)
	link := fmt.Sprintf("http://%v/%v/%v", domain, entitymap[ent], id)
	nl[ent] = Href{link}
	nl["self"] = Href{link}
	*l = nl

}

func (l *Links) AddAttrLink(attr string, corent string, id string) {
	link := fmt.Sprintf("http://%v/%v/%v/%v", domain, entitymap[corent], id, entitymap[attr])
	nl := *l
	nl[entitymap[attr]] = Href{link}
	*l = nl
}

func (l *Links) AddCustomer(id string) {
	l.AddLink("customer", id)
	l.AddAttrLink("address", "customer", id)
	l.AddAttrLink("card", "customer", id)
}

func (l *Links) AddAddress(id string) {
	l.AddLink("address", id)
}

func (l *Links) AddCard(id string) {
	l.AddLink("card", id)
}

type Href struct {
	string `json:"href"`
}
