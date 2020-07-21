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
	"reflect"
	"testing"
)

func TestAddLinksCard(t *testing.T) {
	domain = "mydomain"
	c := Card{ID: "test"}
	c.AddLinks()
	h := Href{"http://mydomain/cards/test"}
	if !reflect.DeepEqual(c.Links["card"], h) {
		t.Error("expected equal address links")
	}

}

func TestMaskCC(t *testing.T) {
	test1 := "1234567890"
	c := Card{LongNum: test1}
	c.MaskCC()
	test1comp := "******7890"
	if c.LongNum != test1comp {
		t.Errorf("Expected matching CC number %v received %v", test1comp, test1)
	}
}
