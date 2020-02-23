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

import * as React from "react";

interface GenericSimpleListProps<T> {
    list: T[];
    show: (element: T) => JSX.Element;
    separate?: boolean | { color: string };
}

type Props<T> = GenericSimpleListProps<T>;

export default class SimpleList<T> extends React.Component<Props<T>, {}> {

    private getSeparatorColor = (): string | undefined => {
        const {separate} = this.props;
        if (typeof separate === 'boolean' && separate) {
            return "black";
        }
        else if (typeof separate === 'object') {
            return separate.color ? separate.color : "black";
        }
    };

    render() {
        const {list, show} = this.props;
        const separatorColor = this.getSeparatorColor();
        return (
            <div>
                {list.map((c, i) => (
                    <div key={i}
                         style={separatorColor && i != list.length - 1 ? {borderBottom: `1px solid ${separatorColor}`} : undefined}>
                        {show(c)}
                    </div>
                ))}
            </div>);
    }
}