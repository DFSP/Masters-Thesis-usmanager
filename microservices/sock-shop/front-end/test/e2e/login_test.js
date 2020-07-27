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

    casper.test.begin("User logs in", 3, function suite(test) {
        casper.start("http://front-end:8080/", function () {
            test.assertNotVisible("#login-modal", "user does not see the login dialogue");

            this.clickLabel("Login");
            casper.waitUntilVisible("#login-modal", function () {
                test.assertVisible("#login-modal", "user is presented with the login dialogue");
                this.fill("#login-modal form", {
                    "username": "Eve_Berger",
                    "password": "duis"
                }, false);
            }, function () {
                test.fail("login dialogue never showed up");
            }, 3000);
        });

        casper.then(function () {
            this.click("#login-modal form button.btn.btn-primary");
            this.waitForText("Logged in as Eve Berger", function () {
                test.pass("user is logged in");
            }, function () {
                test.fail("user login failed");
            }, 3000);
        });

        casper.run(function () {
            test.done();
        });
    });
}());
