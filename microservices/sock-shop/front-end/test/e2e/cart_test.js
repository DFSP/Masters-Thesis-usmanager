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
    require("./config");

    casper.test.begin("User interacts with the cart", 1, function (test) {
        // initial load and login
        casper.start("http://front-end:8080/", function () {
            this.clickLabel("Login");
            this.fill("#login-modal form", {
                "username": "Eve_Berger",
                "password": "duis"
            }, true);
            this.click("#login-modal form button.btn.btn-primary");
            this.waitForText("Logged in", function () {
            }, function () {
                test.fail("login failed");
            }, 3000);
        });

        // access the catalogue
        casper.then(function () {
            this.clickLabel("Catalogue");
        });

        // Add some items to the cart and verify
        casper.then(function () {
            this.waitForText("Add to cart", function () {
                this.clickLabel("Add to cart");
            }, function () {
                test.fail("Catalogue items did not show up");
            }, 3000)

            this.waitForText("1 item(s) in cart", function () {
                test.pass("cart gets updated with user selection");
            }, function () {
                test.fail("cart was not updated");
            }, 3000);
        });


        casper.run(function () {
            test.done();
        });
    });
}());
