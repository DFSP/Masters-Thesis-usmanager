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

import axios, {AxiosResponse} from "axios";
import {API_URL, setupAxiosInterceptors} from "./api";

const USER_NAME_SESSION_ATTRIBUTE_NAME = 'authenticatedUser';

export const basicAuthenticate = (username: string, password: string): Promise<AxiosResponse<any>> =>
    axios.get(`${API_URL}/basicauth`, {
        headers: {
            authorization: createBasicAuthToken(username, password)
        }
    });

export const createBasicAuthToken = (username: string, password: string): string =>
    `Basic ` + window.btoa(username + ":" + password);

export const isAuthenticated = (): boolean => !!sessionStorage.getItem(USER_NAME_SESSION_ATTRIBUTE_NAME);

export const registerSuccessfulLogin = (username: string, password: string): void => {
    sessionStorage.setItem(USER_NAME_SESSION_ATTRIBUTE_NAME, username);
    setupAxiosInterceptors(createBasicAuthToken(username, password))
};

export const getLoggedInUser = () =>
    sessionStorage.getItem(USER_NAME_SESSION_ATTRIBUTE_NAME);

export const logout = () =>
    sessionStorage.removeItem(USER_NAME_SESSION_ATTRIBUTE_NAME);
