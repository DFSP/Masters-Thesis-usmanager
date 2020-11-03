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

    const
        async = require("async"),
        express = require("express"),
        request = require("request"),
        endpoints = require("../endpoints")(),
        helpers = require("../../helpers"),
        app = express(),
        cookie_name = "loggedIn";

    app.get("/customers/:id", function (req, res, next) {
        helpers.simpleHttpRequest(endpoints.userCustomersUrl() + "/" + req.session.customerId, res, next);
    });
    app.get("/cards/:id", function (req, res, next) {
        helpers.simpleHttpRequest(endpoints.userCardsUrl() + "/" + req.params.id, res, next);
    });

    app.get("/customers", function (req, res, next) {
        helpers.simpleHttpRequest(endpoints.userCustomersUrl(), res, next);
    });
    app.get("/addresses", function (req, res, next) {
        helpers.simpleHttpRequest(endpoints.userAddressesUrl(), res, next);
    });
    app.get("/cards", function (req, res, next) {
        helpers.simpleHttpRequest(endpoints.userCardsUrl(), res, next);
    });

    // Create Customer - TO BE USED FOR TESTING ONLY (for now)

    app.post("/customers", function (req, res, next) {
        const options = {
            uri: endpoints.userCustomersUrl(),
            method: 'POST',
            json: true,
            body: req.body
        };
        console.log("Creating Customer: " + JSON.stringify(req.body));
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    app.post("/addresses", function (req, res, next) {
        req.body.userID = helpers.getCustomerId(req, app.get("env"));
        const options = {
            uri: endpoints.userAddressesUrl(),
            method: 'POST',
            json: true,
            body: req.body
        };
        console.log("Creating Address: " + JSON.stringify(req.body));
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    app.get("/card", function (req, res, next) {
        const customerId = helpers.getCustomerId(req, app.get("env"));
        const options = {
            uri: `${endpoints.userCustomersUrl()}/${customerId}/cards`,
            method: 'GET',
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            const data = JSON.parse(body);
            if (data.status_code !== 500 && data._embedded.card.length !== 0) {
                const resp = {
                    "number": data._embedded.card[0].longNum.slice(-4)
                };
                return helpers.respondSuccessBody(res, JSON.stringify(resp));
            }
            return helpers.respondSuccessBody(res, JSON.stringify({"status_code": 500}));
        }.bind({
            res: res
        }));
    });

    app.get("/address", function (req, res, next) {
        const customerId = helpers.getCustomerId(req, app.get("env"));
        const options = {
            uri: `${endpoints.userCustomersUrl()}/${customerId}/addresses`,
            method: 'GET',
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            const data = JSON.parse(body);
            if (data.status_code !== 500 && data._embedded.address.length !== 0) {
                const resp = data._embedded.address[0];
                return helpers.respondSuccessBody(res, JSON.stringify(resp));
            }
            return helpers.respondSuccessBody(res, JSON.stringify({"status_code": 500}));
        }.bind({
            res: res
        }));
    });

    app.post("/cards", function (req, res, next) {
        req.body.userID = helpers.getCustomerId(req, app.get("env"));
        const options = {
            uri: endpoints.userCardsUrl(),
            method: 'POST',
            json: true,
            body: req.body
        };
        console.log("Creating Card: " + JSON.stringify(req.body));
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    // Delete Customer - TO BE USED FOR TESTING ONLY (for now)
    app.delete("/customers/:id", function (req, res, next) {
        const options = {
            uri: `${endpoints.userCustomersUrl()}/${ req.params.id}`,
            method: 'DELETE'
        };
        console.log("Deleting Customer " + req.params.id);
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    // Delete Address - TO BE USED FOR TESTING ONLY (for now)
    app.delete("/addresses/:id", function (req, res, next) {
        console.log("Deleting Address " + req.params.id);
        const options = {
            uri: `${endpoints.userAddressesUrl()}/${ req.params.id}`,
            method: 'DELETE'
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    // Delete Card - TO BE USED FOR TESTING ONLY (for now)
    app.delete("/cards/:id", function (req, res, next) {
        console.log("Deleting Card " + req.params.id);
        const options = {
            uri: `${endpoints.userCardsUrl()}/${req.params.id}`,
            method: 'DELETE'
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            helpers.respondSuccessBody(res, JSON.stringify(body));
        }.bind({
            res: res
        }));
    });

    app.post("/register", function (req, res, next) {
        helpers.sendLocationInfo(req);
        const options = {
            uri: endpoints.userRegisterUrl(),
            method: 'POST',
            json: true,
            body: req.body
        };
        console.log("Registering Customer: " + JSON.stringify(req.body));
        async.waterfall([
                function (callback) {
                    request(options, function (error, response, body) {
                        if (error !== null) {
                            callback(error);
                            return;
                        }
                        if (response.statusCode === 200 && body != null && body !== "") {
                            if (body.error) {
                                callback(body.error);
                                return;
                            }
                            console.log(body);
                            const customerId = body.id;
                            console.log(customerId);
                            req.session.customerId = customerId;
                            callback(null, customerId);
                            return;
                        }
                        console.log(response.statusCode);
                        callback(true);
                    });
                },
                function (customerId, callback) {
                    const sessionId = req.session.id;
                    console.log("Merging carts for customer id: " + customerId + " and session id: " + sessionId);
                    const options = {
                        uri: `${endpoints.cartsUrl()}/${customerId}/merge?sessionId=${sessionId}`,
                        method: 'GET'
                    };
                    request(options, function (error, response, body) {
                        if (error) {
                            if (callback) callback(error);
                            return;
                        }
                        console.log('Carts merged.');
                        if (callback) callback(null, customerId);
                    });
                }
            ],
            function (err, customerId) {
                if (err) {
                    console.log("Error with log in: " + err);
                    res.status(500);
                    res.end();
                    return;
                }
                console.log("set cookie" + customerId);
                res.status(200);
                res.cookie(cookie_name, req.session.id, {
                    maxAge: 3600000
                }).send({id: customerId});
                console.log("Sent cookies.");
                res.end();
            }
        );
    });

    app.get("/login", function (req, res, next) {
        helpers.sendLocationInfo(req);
        console.log("Received login request");
        async.waterfall([
                function (callback) {
                    const options = {
                        headers: {
                            'Authorization': req.get('Authorization')
                        },
                        uri: endpoints.userLoginUrl()
                    };
                    request(options, function (error, response, body) {
                        if (error) {
                            callback(error);
                            return;
                        }
                        if (response.statusCode === 200 && body != null && body !== "") {
                            console.log(body);
                            const customerId = JSON.parse(body).user.id;
                            console.log(customerId);
                            req.session.customerId = customerId;
                            callback(null, customerId);
                            return;
                        }
                        console.log(response.statusCode);
                        callback(true);
                    });
                },
                function (customerId, callback) {
                    const sessionId = req.session.id;
                    console.log("Merging carts for customer id: " + customerId + " and session id: " + sessionId);
                    const options = {
                        uri: `${endpoints.cartsUrl()}/${customerId}/merge?sessionId=${sessionId}`,
                        method: 'GET'
                    };
                    request(options, function (error, response, body) {
                        if (error) {
                            // if cart fails just log it, it prevents login
                            console.log(error);
                            //return;
                        }
                        console.log('Carts merged.');
                        callback(null, customerId);
                    });
                }
            ],
            function (err, customerId) {
                if (err) {
                    console.log("Error with log in: " + err);
                    res.status(401);
                    res.end();
                    return;
                }
                res.status(200);
                res.cookie(cookie_name, req.session.id, {
                    maxAge: 3600000
                }).send('Cookie is set');
                console.log("Sent cookies.");
                res.end();
            });
    });

    module.exports = app;
}());
