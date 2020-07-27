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

var express = require('express'),
    router = express.Router(),
    mongoose = require('mongoose'),
    Movie = require('../models/movie');
var sqrt = require('math-sqrt');
router.get('/api/movies', function (req, res) {
    Movie.find(function (err, movies) {
        if (err) {
            return res.status(500).jsonp({status: 500, message: err.message});
        }
        res.status(200).jsonp(movies);
    });
});

router.get('/api/movie/:id', function (req, res) {
    Movie.findById(req.params.id, function (err, movie) {
        if (err) {
            console.log(res.status(500).jsonp({status: 500, message: err.message}));
        }
        res.status(200).jsonp(movie);
    });
});

router.post('/api/movie', function (req, res) {
    var m = new Movie({
        title: req.body.title,
        year: req.body.year,
        rated: req.body.rated,
        runtime: req.body.runtime,
        genre: req.body.genre,
        director: req.body.director
    });
    m.save(function (err, movie) {
        if (err) {
            return res.status(500).jsonp({status: 500, message: err.message});
        }
        res.status(200).jsonp(movie);
    });
});

router.put('/api/movie/:id', function (req, res) {
    Movie.findById(req.params.id, function (err, movie) {
        movie.title = req.body.title;
        movie.year = req.body.year;
        movie.rated = req.body.rated;
        movie.runtime = req.body.runtime;
        movie.genre = req.body.genre;
        movie.director = req.body.director;
        movie.save(function (err) {
            if (err) {
                return res.status(500).jsonp({status: 500, message: err.message});
            }
            res.status(200).jsonp(movie);
        });
    });
});

router.delete('/api/movie/:id', function (req, res) {
    Movie.findById(req.params.id, function (err, movie) {
        movie.remove(function (err) {
            if (err) {
                return res.status(500).jsonp({status: 500, message: err.message});
            }
            res.status(200).jsonp({status: 200, message: 'Movie deleted.'});
        });
    });
});

module.exports = router;
