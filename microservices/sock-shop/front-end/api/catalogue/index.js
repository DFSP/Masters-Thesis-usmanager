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
        request = require("request"),
        endpoints = require("../endpoints")(),
        helpers = require("../../helpers"),
        app = express();

    app.get("/catalogue/images*",
        function (req, res, next) {
            helpers.sendLocationInfo(req);
            const url = `${endpoints.catalogueUrl()}${req.url.toString()}}`
            request.get(url)
                .on('error', function (e) {
                    next(e);
                })
                .pipe(res);
        });

    app.get("/catalogue*", function (req, res, next) {
        helpers.sendLocationInfo(req);
        const url = `${endpoints.catalogueUrl()}${req.url.toString()}}`
        helpers.simpleHttpRequest(url, res, next);
    });

    app.get("/tags", function (req, res, next) {
        helpers.sendLocationInfo(req);
        const url = endpoints.catalogueTagsUrl();
        helpers.simpleHttpRequest(url, res, next);
    });

    module.exports = app;
}());
