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
	"crypto/sha1"
	"errors"
	"fmt"
	"io"
	"strconv"
	"time"
)

var (
	ErrNoCustomerInResponse = errors.New("Response has no matching customer")
	ErrMissingField         = "Error missing %v"
)

type User struct {
	FirstName string    `json:"firstName" bson:"firstName"`
	LastName  string    `json:"lastName" bson:"lastName"`
	Email     string    `json:"-" bson:"email"`
	Username  string    `json:"username" bson:"username"`
	Password  string    `json:"-" bson:"password,omitempty"`
	Addresses []Address `json:"-,omitempty" bson:"-"`
	Cards     []Card    `json:"-,omitempty" bson:"-"`
	UserID    string    `json:"id" bson:"-"`
	//Links     Links     `json:"_links"`
	Salt string `json:"-" bson:"salt"`
}

func New() User {
	u := User{Addresses: make([]Address, 0), Cards: make([]Card, 0)}
	u.NewSalt()
	return u
}

func (u *User) Validate() error {
	if u.FirstName == "" {
		return fmt.Errorf(ErrMissingField, "FirstName")
	}
	if u.LastName == "" {
		return fmt.Errorf(ErrMissingField, "LastName")
	}
	if u.Username == "" {
		return fmt.Errorf(ErrMissingField, "Username")
	}
	if u.Password == "" {
		return fmt.Errorf(ErrMissingField, "Password")
	}
	return nil
}

func (u *User) MaskCCs() {
	for k, c := range u.Cards {
		c.MaskCC()
		u.Cards[k] = c
	}
}

func (u *User) AddLinks() {
	//u.Links.AddCustomer(u.UserID)
}

func (u *User) NewSalt() {
	h := sha1.New()
	io.WriteString(h, strconv.Itoa(int(time.Now().UnixNano())))
	u.Salt = fmt.Sprintf("%x", h.Sum(nil))
}
