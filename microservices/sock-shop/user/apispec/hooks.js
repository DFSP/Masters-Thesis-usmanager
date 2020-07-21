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

const hooks = require('hooks');

hooks.before("/login > GET", function (transaction, done) {
    transaction.skip = true;
    done();
});

hooks.before("/register > POST", function (transaction, done) {
    transaction.request.headers['Content-Type'] = 'application/json';
    transaction.request.body = JSON.stringify(
        {
            "username": "testuser",
            "password": "testpassword"
        }
    );
    done();
});

hooks.before("/addresses > POST", function (transaction, done) {
    transaction.request.headers['Content-Type'] = 'application/json';
    transaction.request.body = JSON.stringify(
        {
            "street": "teststreet",
            "number": "15",
            "country": "The Netherlands",
            "city": "Den Haag"
        }
    );
    done();
});

hooks.before("/cards > POST", function (transaction, done) {
    transaction.request.headers['Content-Type'] = 'application/json';
    transaction.request.body = JSON.stringify(
        {
            "longNum": "1111222233334444",
            "expires": "11/2020",
            "ccv": "123"
        }
    );
    done();
});
