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
	"fmt"
	"testing"
)

func TestNew(t *testing.T) {
	u := New()
	if len(u.Addresses) != 0 && len(u.Cards) != 0 {
		t.Error("Expected zero length addresses and cards")
	}
}

func TestValidate(t *testing.T) {
	u := New()
	err := u.Validate()
	if err.Error() != fmt.Sprintf(ErrMissingField, "FirstName") {
		t.Error("Expected missing first name error")
	}
	u.FirstName = "test"
	err = u.Validate()
	if err.Error() != fmt.Sprintf(ErrMissingField, "LastName") {
		t.Error("Expected missing last name error")
	}
	u.LastName = "test"
	err = u.Validate()
	if err.Error() != fmt.Sprintf(ErrMissingField, "Username") {
		t.Error("Expected missing username error")
	}
	u.Username = "test"
	err = u.Validate()
	if err.Error() != fmt.Sprintf(ErrMissingField, "Password") {
		t.Error("Expected missing password error")
	}
	u.Password = "test"
	err = u.Validate()
	if err != nil {
		t.Error(err)
	}
}

func TestMaskCCs(t *testing.T) {
	u := New()
	u.Cards = append(u.Cards, Card{LongNum: "abcdefg"})
	u.Cards = append(u.Cards, Card{LongNum: "hijklmnopqrs"})
	u.MaskCCs()
	if u.Cards[0].LongNum != "***defg" {
		t.Error("Card one CC not masked")
	}
	if u.Cards[1].LongNum != "********pqrs" {
		t.Error("Card two CC not masked")
	}
}
