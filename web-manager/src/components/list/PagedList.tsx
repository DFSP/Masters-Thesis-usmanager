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
import SimpleList from './SimpleList';
import './PagedList.css';
import {Dropdown} from "../form/Dropdown";
import Pagination from "./Pagination";

export interface IPagedList<T> {
  list: T[];
  show: (element: T, index: number, last: boolean) => JSX.Element;
  paginate: {
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

interface State {
  max: number;
  pagesize: number;
  page: number;
}

export class PagedList<T> extends React.Component<IPagedList<T>, State> {

  private max: number = 0;

  constructor(props: IPagedList<T>) {
    super(props);
    const initialMax = Math.max(0, Math.ceil(props.list.length / (props.paginate.pagesize.initial || 1)) - 1);
    this.state = {
      max: initialMax,
      page: (props.paginate.page?.last ? initialMax : props.paginate.page?.index) || 0,
      pagesize: props.paginate.pagesize.initial,
    };
  }

  public render() {
    const {list: l, show, paginate} = this.props;
    const {page = 0, pagesize = l.length} = this.state;
    const list = l.slice(page * pagesize, page * pagesize + pagesize);
    const pagination = <Pagination max={this.state.max}
                                   page={page}
                                   setPage={this.setPage}
                                   prevPage={this.prevPage}
                                   nextPage={this.nextPage}/>;
    // TODO: page transition. e.g. slide to the left while new page comes from the right. Using react spring
    return (
      <div className={'list'}>
        {paginate.pagesize.options && (
          <div className={'pageSize'}>
            <Dropdown<(number | 'all')>
              id={'pageSize'}
              name={'pageSize'}
              value={this.state.pagesize === Number.MAX_VALUE ? 'all' : this.state.pagesize?.toString()}
              onChange={this.setPageSize}
              dropdown={{
                defaultValue: 'Page size',
                values: paginate.pagesize.options,
                optionToString: this.pageSizeOption
              }}>
            </Dropdown>
          </div>
        )}
        {(paginate.position === undefined || paginate.position === 'top' || paginate.position === 'top-bottom')
         && this.state.max > 0 && pagination}
        <SimpleList<T> {...this.props} list={list} show={show}/>
        {(paginate.position === 'bottom' || paginate.position === 'top-bottom')
         && this.state.max > 0 && pagination}
      </div>
    );
  }

  private setPage = (pageIndex: number): void => {
    this.setState(state => ({
      page: state.page === undefined ? 0 : Math.max(0, pageIndex),
      max: Math.max(0, Math.ceil(this.props.list.length / this.state.pagesize) - 1)
    }));
    window.scrollTo(0, 0);
  };

  private prevPage = (): void => {
    const {page} = this.state;
    this.setPage(page === undefined ? 0 : Math.max(0, page - 1));
  };

  private nextPage = (): void => {
    const {page} = this.state;
    this.setPage(page === undefined ? 0 : Math.min(this.state.max, page + 1));
  };

  private setPageSize = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedPageSize = e.currentTarget.value;
    let pagesize: number;
    if (selectedPageSize === 'all') {
      pagesize = Number.MAX_VALUE;
    } else {
      pagesize = parseInt(selectedPageSize);
    }
    const max = Math.max(0, Math.ceil(this.props.list.length / pagesize) - 1);
    this.setState({
      pagesize,
      max,
      page: max,
    })
  };

  private pageSizeOption = (option: (number | 'all')): string =>
    option.toString();
}
