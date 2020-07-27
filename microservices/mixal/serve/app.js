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

const express = require('express');
const config = require('./config');
var rp = require('request-promise');
var Promise = require("bluebird");
const request = Promise.promisifyAll(require('request'));
/**
 * javascript promises for join function
 */
const join = require("bluebird").join;

const app = express();

function getPrime() {

    var url = 'http://' + config.primeapp.host + ':' + config.primeapp.port + '/api/test';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
        'proxy': process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}

function getMovies() {

    var url = 'http://' + config.movieapp.host + ':' + config.movieapp.port + '/api/movies';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
        'proxy': process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}

function getWeb() {

    var url = 'http://' + config.webacapp.host + ':' + config.webacapp.port + '/api/web';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
        'proxy': process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}

const router = express.Router();
/**
 * Middleware to use for all requests
 */
router.use(function (req, res, next) {
    /**
     * Logs can be printed here while accessing any routes
     */
    console.log('Accessing Serve Routes');
    next();
});
/**
 * Base route of the router : to make sure everything is working check http://localhost:8080/exercises)
 */
router.get('/', function (req, res) {
    res.json({message: 'Welcome to Serve API!'});
});

router.route('/test')
    .get(function (req, res) {
        Promise.all([getMovies(),
            getPrime(),
            getWeb()])
            .spread(function (resultMovi, resultPrime, resultWeb) {
                var ex3_response_message = {
                    "movie": resultMovi,
                    "prime": resultPrime,
                    "productURL": resultWeb
                };
                res.setHeader('Content-Type', 'application/json');
                res.json(ex3_response_message);
            })
    });
/**
 * REGISTER OUR ROUTES
 * our router is now pointing to /exercises
 */
app.use('/', router);


module.exports = app;

