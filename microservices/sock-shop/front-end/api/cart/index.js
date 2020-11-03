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
        helpers = require("../../helpers"),
        endpoints = require("../endpoints")(),
        app = express();

    // List items in cart for current logged in user
    app.get("/cart", function (req, res, next) {
        const customerId = helpers.getCustomerId(req, app.get("env"));
        console.log("Request received: " + req.url + ", " + req.query.customerId);
        console.log("Customer ID: " + customerId);
        request(`${endpoints.cartsUrl()}/${customerId}/items`,
            function (error, response, body) {
                if (error) {
                    return next(error);
                }
                helpers.respondStatusBody(res, response.statusCode, body)
            });
    });

    // Delete cart
    app.delete("/cart", function (req, res, next) {
        const customerId = helpers.getCustomerId(req, app.get("env"));
        console.log('Attempting to delete cart for user: ' + customerId);
        const options = {
            uri: `${endpoints.cartsUrl()}/${customerId}`,
            method: 'DELETE'
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            console.log('User cart deleted with status: ' + response.statusCode);
            helpers.respondStatus(res, response.statusCode);
        });
    });

    // Delete item from cart
    app.delete("/cart/:id", function (req, res, next) {
        if (req.params.id == null) {
            return next(new Error("Must pass id of item to delete"), 400);
        }
        const customerId = helpers.getCustomerId(req, app.get("env"));
        console.log("Delete item from cart " + req.url + " for user " + customerId)
        const options = {
            uri: endpoints.cartsUrl() + "/" + customerId + "/items/" + req.params.id.toString(),
            method: 'DELETE'
        };
        request(options, function (error, response, body) {
            if (error) {
                return next(error);
            }
            console.log('Item deleted with status: ' + response.statusCode);
            helpers.respondStatus(res, response.statusCode);
        });
    });

    // Add new item to cart
    app.post("/cart", function (req, res, next) {
        console.log("Attempting to add to cart: " + JSON.stringify(req.body));
        if (req.body.id == null) {
            next(new Error("Must pass id of item to add"), 400);
            return;
        }
        const customerId = helpers.getCustomerId(req, app.get("env"));

        async.waterfall([
            function (callback) {
                request(`${endpoints.catalogueUrl()}/catalogue/${req.body.id.toString()}`,
                    function (error, response, body) {
                        console.log(body);
                        callback(error, JSON.parse(body));
                    });
            },
            function (item, callback) {
                const options = {
                    uri: `${endpoints.cartsUrl()}/${customerId}/items"`,
                    method: 'POST',
                    json: true,
                    body: {itemId: item.id, unitPrice: item.price}
                };
                console.log("POST to carts: " + options.uri + " body: " + JSON.stringify(options.body));
                request(options, function (error, response, body) {
                    if (error) {
                        callback(error)
                        return;
                    }
                    callback(null, response.statusCode);
                });
            }
        ], function (err, statusCode) {
            if (err) {
                return next(err);
            }
            if (statusCode !== 201) {
                return next(new Error("Unable to add to cart. Status code: " + statusCode))
            }
            helpers.respondStatus(res, statusCode);
        });
    });

    // Update cart item
    app.post("/cart/update", function (req, res, next) {
        console.log("Attempting to update cart item: " + JSON.stringify(req.body));
        if (req.body.id == null) {
            next(new Error("Must pass id of item to update"), 400);
            return;
        }
        if (req.body.quantity == null) {
            next(new Error("Must pass quantity to update"), 400);
            return;
        }
        const customer = helpers.getCustomerId(req, app.get("env"));

        async.waterfall([
            function (callback) {
                request(endpoints.catalogueUrl() + "/catalogue/" + req.body.id.toString(), function (error, response, body) {
                    console.log(body);
                    callback(error, JSON.parse(body));
                });
            },
            function (item, callback) {
                const options = {
                    uri: `${endpoints.cartsUrl()}/${customer}/items`,
                    method: 'PATCH',
                    json: true,
                    body: {itemId: item.id, quantity: parseInt(req.body.quantity), unitPrice: item.price}
                };
                console.log("PATCH to carts: " + options.uri + " body: " + JSON.stringify(options.body));
                request(options, function (error, response, body) {
                    if (error) {
                        callback(error)
                        return;
                    }
                    callback(null, response.statusCode);
                });
            }
        ], function (err, statusCode) {
            if (err) {
                return next(err);
            }
            if (statusCode !== 202) {
                return next(new Error("Unable to add to cart. Status code: " + statusCode))
            }
            helpers.respondStatus(res, statusCode);
        });
    });

    module.exports = app;
}());
