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

export interface IPagedList<T> {
    empty: string | (() => JSX.Element);
    list: T[];
    show: (x: T) => JSX.Element;
    pagination: { pagesize: number, page?: number, bottom?: boolean };
    separator?: boolean | { color: string };
}

interface State {
    pagesize?: number;
    page?: number;
}

export class PagedList<T> extends React.Component<IPagedList<T>, State> {

    private max: number = 0;

    constructor(props: IPagedList<T>) {
        super(props);
        this.state = {
            page: props.pagination.page || 0,
            pagesize: props.pagination.pagesize,
        };
        this.max = Math.max(0, Math.ceil(props.list.length / (this.state.pagesize || 1)) - 1);
    }

    public render() {
        const {empty, list: l, show, pagination, separator} = this.props;
        const {page = 0, pagesize = l.length} = this.state;
        const list = l.slice(page * pagesize, page * pagesize + pagesize);
        return (
            <div>
                <ul className="pagination center-align" style={{userSelect: "none"}}>
                    {!pagination.bottom &&
                    <li className={page === 0 ? "disabled prev" : "prev"}>
                        <a onClick={this.prevPage}>
                            <i className="material-icons">chevron_left</i>
                        </a>
                    </li>}
                    {Array.from({length: this.max + 1}, (x, i) => i+1).map((pageNumber, index) =>
                        <PageNumber key={index} page={pageNumber} active={index === page} setPage={this.setPage}/>
                    )}
                    <li className={page === this.max ? "disabled next" : "next"}>
                        <a onClick={this.nextPage}>
                            <i className="material-icons">chevron_right</i>
                        </a>
                    </li>
                    {pagination.bottom &&
                    <li className={page === 0 ? "disabled prev" : "prev"}>
                        <a onClick={this.prevPage}>
                            <i className="material-icons">chevron_left</i>
                        </a>
                    </li>}
                </ul>
                <SimpleList<T> {...{empty, list, show, separator}}/>
            </div>
        );
    }

    private setPage = (pageIndex: number): void => {
        this.max = Math.max(0, Math.ceil(this.props.list.length / (this.state.pagesize || 1)) - 1);
        this.setState(state => ({page: state.page === undefined ? 0 : Math.max(0, pageIndex)}));
    };

    private prevPage = (): void => {
        const {page} = this.state;
        this.setPage(page === undefined ? 0 : Math.max(0, page - 1));
    };

    private nextPage = (): void => {
        const {page} = this.state;
        this.setPage(page === undefined ? 0 : Math.min(this.max, page + 1));
    };

}

interface PageNumberProps {
    page: number;
    active: boolean;
    setPage: (pageIndex: number) => void;
}

class PageNumber extends React.Component<PageNumberProps, {}> {

    private changePage = () => {
        this.props.setPage(this.props.page - 1);
    };

    public render = () => {
        const {page, active} = this.props;
        return <li key={page} className={active ? "active" : "waves-effect"}>
            <a onClick={this.changePage}>{page}</a>
        </li>
    };
}