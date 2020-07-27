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

(function () {
    'use strict';

    var expect = require("chai").expect
        , endpoints = require("../../api/endpoints")

    describe("endpoints", function () {
        describe("catalogueUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.catalogueUrl).to.equal("http://catalogue");
            });
        });

        describe("tagsUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.tagsUrl).to.equal("http://catalogue/tags");
            });
        });

        describe("cartsUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.cartsUrl).to.equal("http://carts/carts");
            });
        });

        describe("ordersUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.ordersUrl).to.equal("http://orders");
            });
        });

        describe("customersUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.customersUrl).to.equal("http://user/customers");
            });
        });

        describe("addressUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.addressUrl).to.equal("http://user/addresses");
            });
        });

        describe("cardsUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.cardsUrl).to.equal("http://user/cards");
            });
        });

        describe("loginUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.loginUrl).to.equal("http://user/login");
            });
        });

        describe("registerUrl", function () {
            it("points to the proper endpoint", function () {
                expect(endpoints.registerUrl).to.equal("http://user/register");
            });
        });
    });
}());
