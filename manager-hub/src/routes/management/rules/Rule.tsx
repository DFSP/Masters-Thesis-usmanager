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

import IDatabaseData from "../../../components/IDatabaseData";

export interface IRule extends IDatabaseData {
    name: string;
    priority: number;
    generic: boolean;
    decision: IDecision;
    conditions: string[];
}

export interface IDecision extends IDatabaseData {
    ruleDecision: string;
    componentType: IComponentType;
}

interface IComponentType extends IDatabaseData {
    type: string;
}

export const componentTypes = {
    HOST: {type: "host"},
    SERVICE: {type: "service"},
    CONTAINER: {type: "container"}
};

export interface IValueMode extends IDatabaseData {
    name: string;
}

export interface IField extends IDatabaseData {
    name: string;
    query: IPrometheusQuery;
}

export interface IPrometheusQuery {
    query: string;
}

export interface IOperator extends IDatabaseData {
    operator: string;
    symbol: string;
}
