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

import React from "react";
import {connect} from "react-redux";
import {PagedList} from "./PagedList";
import SimpleList from "./SimpleList";
import {ReduxState} from "../../reducers";
import Empty from "./Empty";

interface FilteredListProps<T> {
  list: T[];
  show: (element: T, index: number, last: boolean) => JSX.Element;
  predicate: (x: T, s: string) => boolean;
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
}

interface StateToProps {
  search: string;
}

type Props<T> = FilteredListProps<T> & StateToProps;

class GenericFilteredList<T> extends React.Component<Props<T>, {}> {

  public render() {
    const {list, predicate, search, paginate, ...otherProps} = this.props;
    const filteredList = list.filter((s: T) => predicate(s, search));
    if (list.length !== filteredList.length && filteredList.length === 0) {
      return <Empty message={search} error={'No matches found'}/>;
    }
    if (paginate) {
      return <PagedList {...otherProps} list={filteredList} paginate={paginate}/>
    }
    return <SimpleList<T> {...otherProps} list={filteredList}/>
  }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    search: state.ui.search.toLowerCase()
  }
);

export default function FilteredList<T>() {
  return connect(mapStateToProps)(GenericFilteredList as new(props: Props<T>) => GenericFilteredList<T>);
}
