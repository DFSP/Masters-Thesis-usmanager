(function (){
  'use strict';

  var util  = require('util')
  const axios = require("axios");
  var request = require('sync-request');

  // TODO : change
  const defaultGetAppByNameURL = 'http://localhost:1906/api/apps/'

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
  }

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
