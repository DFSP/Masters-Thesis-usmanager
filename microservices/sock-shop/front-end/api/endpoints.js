/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

(function (){
  'use strict';

  var util  = require('util');
  const axios = require("axios");
  var request = require('sync-request');

  // TODO : change
  const defaultGetAppByNameURL = 'http://localhost:1906/api/apps/';

  module.exports = function () {

    module.catalogueUrl = function () { return getCatalogueUrl() },
    module.tagsUrl      = function () { return getCatalogueUrl() + '/tags' },
    module.cartsUrl     = function () { return getCartsUrl() + '/carts' },
    module.ordersUrl    = function () { return getOrdersUrl() },
    module.customersUrl = function () { return getUserUrl() + '/customers' },
    module.addressUrl   = function () { return getUserUrl() + '/addresses' },
    module.cardsUrl     = function () { return getUserUrl() + '/cards' },
    module.loginUrl     = function () { return getUserUrl() + '/login' },
    module.registerUrl  = function () { return getUserUrl() + '/register' };

    return module;
  };

  async function getAppEndpointByNameAsync (appName) {
    try {
      const response = await axios.get(defaultGetAppByNameURL + appName);
      const data = response.data;
      return data.endpoint
    } catch (error) {
      console.log("Request getAppEndpointByName " + error);
      return ''
    }
  }

  // TODO : change to the async version 
  function getAppEndpointByNameSync (appName) {
    try {
      var res = request('GET', defaultGetAppByNameURL + appName);
      const resData = JSON.parse(res.getBody('utf8'));
      return resData.endpoint
    } catch (error) {
      console.log("Request getAppEndpointByName " + error);
      return ''
    }
  }

  function getCatalogueUrl() {
    return getAppEndpointByNameSync('CATALOGUE')
  }

  function getCartsUrl() {
    return getAppEndpointByNameSync('CARTS')
  }

  function getOrdersUrl() {
    return getAppEndpointByNameSync('ORDERS')
  }

  function getUserUrl() {
    return getAppEndpointByNameSync('USER')   
  }

}());
