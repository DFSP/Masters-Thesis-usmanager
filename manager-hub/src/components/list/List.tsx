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

import * as React from 'react';
import FilteredList from './FilteredList';
import {PagedList} from "./PagedList";
import SimpleList from "./SimpleList";
import LoadingSpinner from "./LoadingSpinner";
import {Error} from "../errors/Error";
import AnimatedList from "./AnimatedList";
import Empty from "./Empty";

interface ListProps<T> {
    isLoading?: boolean;
    error?: string | null;
    emptyMessage?: string;
    list: T[];
    show: (element: T, index: number, last: boolean) => JSX.Element;
    predicate?: (element: T, filter: string) => boolean;
    paginate?: {
        pagesize: {
            initial: number,
            options?: ('all' | number)[],
        },
        page?: {
            index?: number,
            last?: boolean
        },
        position?: 'top' | 'bottom' | 'top-bottom';
    };
    animate?: boolean;
    header?: () => JSX.Element;
}

type Props<T> = ListProps<T>;

class GenericList<T> extends React.Component<Props<T>, {}> {

    public render() {
        const {isLoading, error, emptyMessage, list, predicate, paginate, animate} = this.props;
        if (isLoading) {
            return <LoadingSpinner/>;
        }
        if (error) {
            return <Error message={error}/>;
        }
        if (list.length === 0 && emptyMessage) {
            return <Empty message={emptyMessage}/>
        }
        if (predicate) {
            const ListFiltered = FilteredList<T>();
            return <ListFiltered {...this.props} predicate={predicate}/>
        }
        if (paginate) {
            return <PagedList {...this.props} paginate={paginate}/>
        }
        if (animate) {
            return <AnimatedList {...this.props} />
        }
        return <SimpleList<T> {...this.props}/>
    }

}

export default function List<T>() {
    return (GenericList as new(props: Props<T>) => GenericList<T>);
}
