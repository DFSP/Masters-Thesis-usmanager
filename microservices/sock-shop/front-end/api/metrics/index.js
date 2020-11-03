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

    const express = require("express"),
        client = require('prom-client'),
        app = express();

    const metric = {
        http: {
            requests: {
                duration: new client.Histogram('request_duration_seconds', 'request duration in seconds', ['service', 'method', 'route', 'status_code']),
            }
        }
    }

    function s(start) {
        const diff = process.hrtime(start);
        return (diff[0] * 1e9 + diff[1]) / 1000000000;
    }

    function observe(method, path, statusCode, start) {
        const route = path.toLowerCase();
        if (route !== '/metrics' && route !== '/metrics/') {
            const duration = s(start);
            method = method.toLowerCase();
            metric.http.requests.duration.labels('front-end', method, route, statusCode).observe(duration);
        }
    }

    function middleware(request, response, done) {
        const start = process.hrtime();
        response.on('finish', function () {
            observe(request.method, request.path, response.statusCode, start);
        });
        return done();
    }

    app.use(middleware);
    app.get("/metrics", function (req, res) {
        res.header("content-type", "text/plain");
        return res.end(client.register.metrics())
    });

    module.exports = app;
}());
