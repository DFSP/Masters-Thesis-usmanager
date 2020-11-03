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

    const request = require("request");
    const helpers = {};

    /* Public: errorHandler is a middleware that handles your errors
     *
     * Example:
     *
     * var app = express();
     * app.use(helpers.errorHandler);
     * */

    helpers.errorHandler = function (err, req, res, next) {
        const ret = {
            message: err.message,
            error: err
        };
        res.status(err.status || 500).send(ret);
    };

    helpers.sessionMiddleware = function (err, req, res, next) {
        if (!req.cookies.loggedIn) {
            res.session.customerId = null;
        }
    };

    /* Responds with the given body and status 200 OK  */
    helpers.respondSuccessBody = function (res, body) {
        helpers.respondStatusBody(res, 200, body);
    }

    /* Public: responds with the given body and status
     *
     * res        - response object to use as output
     * statusCode - the HTTP status code to set to the response
     * body       - (string) the body to yield to the response
     */
    helpers.respondStatusBody = function (res, statusCode, body) {
        res.writeHeader(statusCode);
        res.write(body);
        res.end();
    }

    /* Responds with the given statusCode */
    helpers.respondStatus = function (res, statusCode) {
        res.writeHeader(statusCode);
        res.end();
    }

    /* Rewrites and redirects any url that doesn't end with a slash. */
    helpers.rewriteSlash = function (req, res, next) {
        if (req.url.substr(-1) === '/' && req.url.length > 1)
            res.redirect(301, req.url.slice(0, -1));
        else
            next();
    }

    /* Public: performs an HTTP GET request to the given URL
     *
     * url  - the URL where the external service can be reached out
     * res  - the response object where the external service's output will be yield
     * next - callback to be invoked in case of error. If there actually is an error
     *        this function will be called, passing the error object as an argument
     *
     * Examples:
     *
     * app.get("/users", function(req, res) {
     *   helpers.simpleHttpRequest("http://api.example.org/users", res, function(err) {
     *     res.send({ error: err });
     *     res.end();
     *   });
     * });
     */
    helpers.simpleHttpRequest = function (url, res, next) {
        request.get(url, function (error, response, body) {
            if (error) return next(error);
            helpers.respondSuccessBody(res, body);
        }.bind({res: res}));
    }

    /* Get customer id from cookies or session id */
    helpers.getCustomerId = function (req, env) {
        // Check if logged in. Get customer Id
        const loggedIn = req.cookies.loggedIn;

        // TODO REMOVE THIS, SECURITY RISK
        if (env === "development" && req.query.customerId != null) {
            return req.query.customerId;
        }

        if (!loggedIn) {
            if (!req.session.id) {
                throw new Error("User not logged in.");
            }
            // Use Session ID instead
            return req.session.id;
        }

        return req.session.customerId;
    }

    helpers.sendLocationInfo = function (req) {
        let latitude = req.headers.x_latitude;
        let longitude = req.headers.x_longitude;

        if (latitude > 0 && longitude > 0) {
            const options = {
                uri: 'http://localhost:1906/api/metrics',
                method: 'POST',
                json: {
                    "service": "sock-shop-front-end",
                    "latitude": latitude,
                    "longitude": longitude,
                    "count": 1
                }
            };
            console.log("Send " + options + " to location info service")
            request(options, function (error, response, body) {
                if (error) {
                    console.error("Error sending location info to request-location-monitor...");
                }
                console.log("Response body: " + JSON.stringify(body));
            });
        }

    }

    module.exports = helpers;
}());
