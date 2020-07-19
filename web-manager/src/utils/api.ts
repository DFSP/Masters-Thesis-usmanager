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

import axios, {AxiosError, AxiosRequestConfig, AxiosResponse, CancelTokenSource, Method} from "axios";
import {isAuthenticated} from "./auth";
import {camelCaseToSentenceCase, snakeCaseToCamelCase} from "./text";

export interface IReply<T> extends AxiosResponse<T> {

}

export const API_URL = 'http://localhost:8080';
export const REQUEST_TIMEOUT = 300000;
const CancelToken = axios.CancelToken;
export const cancelRequests: { [key: string]: CancelTokenSource } = {};

//TODO delete
export function getData<T>(url: string, callback: (data: T) => void): any {
  fetch(url, {
    method: 'GET',
    headers: new Headers({
      'Authorization': 'Basic YWRtaW46YWRtaW4=',
      'Content-type': 'application/json;charset=UTF-8',
      'Accept': 'application/json;charset=UTF-8',
    }),
  }).then(response => {
    if (response.ok) {
      return response.json();
    }
    throw new Error(`${response.status} ${response.statusText}`);
  }).then(json => {
    callback(json);
  }).catch(e => {
    console.log(e);
  });
}

export function postData<T>(url: string, requestBody: any,
                            successCallback: (response: IReply<T>) => void, failureCallback: (reason: string) => void): void {
  sendData(url, 'POST', requestBody, successCallback, failureCallback);
}

export function putData<T>(url: string, requestBody: any,
                           successCallback: (response: any) => void, failureCallback: (reason: string) => void): void {
  sendData<T>(url, 'PUT', requestBody, successCallback, failureCallback);
}

export function patchData<T>(url: string, requestBody: any,
                             successCallback: (response: IReply<T>) => void, failureCallback: (reason: string) => void,
                             action?: "post" | "delete",): void {
  if (action) {
    requestBody = {request: action.toUpperCase(), body: requestBody};
  }
  sendData<T>(url, 'PATCH', requestBody, successCallback, failureCallback);
}

function sendData<T>(endpoint: string, method: Method, data: any,
                     successCallback: (response: IReply<T>) => void, failureCallback: (reason: string) => void) {
  const url = new URL(endpoint.includes(API_URL) ? endpoint : `${API_URL}/${endpoint}`);
  console.log(`${method} ${url} ${JSON.stringify(data)}`);
  axios(url.href, {
    method,
    headers: {
      //TODO remove headers
      'Authorization': 'Basic YWRtaW46YWRtaW4=',
      'Content-type': 'application/json;charset=UTF-8',
      'Accept': 'application/json;charset=UTF-8',
    },
    data,
    //TODO remove
    timeout: REQUEST_TIMEOUT,
    //TODO remove
    cancelToken: setCancelRequest(method, url.pathname).token
  }).then((response: AxiosResponse) => {
    deleteCancelRequest(method, url.pathname);
    console.log(response);
    successCallback(response)
  }).catch((error: AxiosError) => {
    deleteCancelRequest(method, url.pathname);
    if (axios.isCancel(error)) {
      console.log(error.message || 'Request canceled');
    } else {
      const reason = buildErrorMessage(error);
      console.error(reason);
      failureCallback(reason);
    }
  })
}

export function deleteData(endpoint: string,
                           successCallback: () => void,
                           failureCallback: (reason: string) => void,
                           data?: any): void {
  const url = new URL(endpoint.includes(API_URL) ? endpoint : `${API_URL}/${endpoint}`);
  console.log(`DELETE ${url}`);
  axios.delete(url.href, {
    // TODO set options from setupAxiosInterceptors instead, after login
    headers: {
      'Authorization': 'Basic YWRtaW46YWRtaW4=', //TODO remove
      'Content-type': 'application/json;charset=UTF-8',
      'Accept': 'application/json;charset=UTF-8',
    },
    data,
    timeout: REQUEST_TIMEOUT,
    cancelToken: setCancelRequest('delete', url.pathname).token
  }).then((response: AxiosResponse) => {
    deleteCancelRequest('delete', url.pathname);
    console.log(response);
    successCallback();
  }).catch((error: AxiosError) => {
    deleteCancelRequest('delete', url.pathname);
    if (axios.isCancel(error)) {
      console.log(error.message || 'Request canceled');
    } else {
      const reason = buildErrorMessage(error);
      console.error(reason);
      failureCallback(reason);
    }
  })
}

function buildErrorMessage(error: AxiosError): string {
  let responseMessage = error.response?.data.apierror?.message;
  if (!responseMessage) {
    return error.message;
  }
  const responseStatusCode = error.response?.status;
  const responseStatusMessage = camelCaseToSentenceCase(snakeCaseToCamelCase(error.response?.data.apierror.status.toLowerCase()));
  return `${responseStatusCode} ${responseStatusMessage} - ${responseMessage}`;
}

const buildCancelRequest = (method: Method, url: string) =>
  `${method}:${url.startsWith('/') ? url : '/' + url}`.toLowerCase();

export const setCancelRequest = (method: Method, url: string): CancelTokenSource => {
  return cancelRequests[buildCancelRequest(method, url)] = CancelToken.source();
};

export const getCancelRequest = (method: Method, url: string): CancelTokenSource =>
  cancelRequests[buildCancelRequest(method, url)];

export const deleteCancelRequest = (method: Method, url: string): boolean =>
  delete cancelRequests[buildCancelRequest(method, url)];

export const setupAxiosInterceptors = (token: string): void => {
  axios.interceptors.request.use(
    (config: AxiosRequestConfig) => {
      if (isAuthenticated()) {
        config.headers['Authorization'] = token;
      }
      config.headers['Content-Type'] = 'application/json;charset=UTF-8';
      config.headers['Accept'] = 'application/json;charset=UTF-8';
      config.timeout = REQUEST_TIMEOUT;
      if (config.method && config.url) {
        config.cancelToken = setCancelRequest(config.method, config.url).token;
      }
      return config
    }
  )
};
