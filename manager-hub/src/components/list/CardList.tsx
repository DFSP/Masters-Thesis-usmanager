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

import List from "./List";
import React from "react";

interface Props<T> {
    isLoading: boolean;
    error?: string | null;
    emptyMessage: string;
    list: T[];
    card: (element: T) => JSX.Element;
    predicate?: (item: T, filter: string) => boolean;
}

interface State {
    pagesize: number;
}

export default class CardList<T> extends React.Component<Props<T>, State> {

    constructor(props: Props<T>) {
        super(props);
        this.state = {
            pagesize: this.calcPagesize()
        }
    }

    public componentDidMount(): void {
        window.addEventListener('resize', this.handleResize);
    }

    public componentWillUnmount(): void {
        window.removeEventListener('resize', this.handleResize);
    }

    public render() {
        const GenericList = List<T>();
        return <GenericList {...this.props}
                            show={this.props.card}
                            paginate={{pagesize: {initial: this.state.pagesize}}}/>
    }

    private handleResize = (_: Event) => {
        this.setState({pagesize: this.calcPagesize()})
    };

    private calcPagesize = () => {
        const width = window.innerWidth;
        if (width <= 600) {
            return 4;
        } else if (width <= 992) {
            return 6;
        } else {
            return 8;
        }
    };

}
