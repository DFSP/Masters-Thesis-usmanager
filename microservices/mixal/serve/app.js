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

function getPrime(){

    var url = 'http://'+config.primeapp.host + ':' + config.primeapp.port + '/api/test';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
		'proxy':process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}
function getMovies(){

    var url = 'http://'+config.movieapp.host + ':' + config.movieapp.port + '/api/movies';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
        'proxy':process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}
function getWeb(){

    var url = 'http://'+config.webacapp.host + ':' + config.webacapp.port + '/api/web';
    console.log(url);
    var options = {
        uri: url,
        method: 'GET',
        headers: {
            'Content-type': 'application/json',
        },
        'proxy':process.env.HTTP_PROXY,
        json: true // Automatically parses the JSON string in the response
    };

    return rp(options);
}

const router = express.Router();
/**
 * Middleware to use for all requests
 */
router.use(function(req, res, next) {
    /**
     * Logs can be printed here while accessing any routes
     */
    console.log('Accessing Serve Routes');
    next();
});
/**
 * Base route of the router : to make sure everything is working check http://localhost:8080/exercises)
 */
router.get('/', function(req, res) {
    res.json({ message: 'Welcome to Serve API!'});
});

router.route('/test')
    .get(function(req, res)
    {
        Promise.all( [getMovies(),
            getPrime(),
            getWeb()])
            .spread (function (resultMovi, resultPrime, resultWeb) {
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

