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

import * as React from "react";
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";
import {PageNumber} from "./PageNumber";
import styles from './Pagination.module.css';

interface StateToProps {
    sidenavVisible: boolean;
}

interface PaginationProps {
    max: number;
    page: number;
    setPage: (pageIndex: number) => void;
    prevPage: () => void;
    nextPage: () => void;
}

type Props = StateToProps & PaginationProps;

class Pagination extends React.Component<Props, {}> {

    public render() {
        const {max, page, setPage, prevPage, nextPage, sidenavVisible} = this.props;
        const needsEllipsis = max >= 10;
        let pagination;
        if (!needsEllipsis) {
            const noEllipsis: number[] = this.noEllipsis(max);
            pagination = noEllipsis.map((pageNumber, index) =>
                <PageNumber key={index} page={pageNumber} active={index === page} setPage={setPage}/>
            )
        } else {
            const beforeEllipsis: number[] = this.beforeEllipsis(max, page);
            const betweenEllipsis: number[] = this.betweenEllipsis(max, page);
            const afterEllipsis: number[] = this.afterEllipsis(max, page);
            pagination =
                <>
                    {beforeEllipsis.map((pageNumber, index) =>
                        <PageNumber key={index} page={pageNumber} active={index === page} setPage={setPage}/>
                    )}
                    {beforeEllipsis.length === 1 && (
                        <>
                            <i className={`material-icons bottom three-dots`}>more_horiz</i>
                            {afterEllipsis.length === 1 && (
                                betweenEllipsis.map((pageNumber, index) =>
                                    <PageNumber key={index} page={pageNumber} active={true}
                                                setPage={setPage}/>
                                )
                            )}
                        </>
                    )}
                    {afterEllipsis.length === 1 && (
                        <i className={`material-icons bottom three-dots`}>more_horiz</i>
                    )}
                    {afterEllipsis.map((pageNumber, index) =>
                        <PageNumber key={index} page={pageNumber}
                                    active={max - (afterEllipsis.length - index) + 1 === page}
                                    setPage={setPage}/>
                    )}
                </>
        }
        return (
            <ul className='pagination no-select'
                style={sidenavVisible ? {left: "100px"} : undefined}>
                <li className={page === 0 ? "disabled" : undefined}>
                    <a onClick={prevPage}>
                        <i className="material-icons small">chevron_left</i>
                    </a>
                </li>
                {pagination}
                <li className={page === this.props.max ? "disabled" : undefined}>
                    <a onClick={nextPage}>
                        <i className="material-icons small">chevron_right</i>
                    </a>
                </li>
            </ul>
        )
    }

    private noEllipsis = (max: number) =>
        Array.from({length: max + 1}, (x, i) => i + 1);

    private beforeEllipsis = (max: number, page: number): number[] =>
        Array.from({length: page < 3 ? 3 : 1}, (x, i) => i + 1);

    private afterEllipsis = (max: number, page: number): number[] =>
        Array.from({length: page > max - 3 ? 3 : 1}, (x, i) => max - i + 1).reverse();

    private betweenEllipsis = (max: number, page: number): number[] =>
        Array.from({length: 1}, (x, i) => page + 1).reverse();
}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
    }
);

export default connect(mapStateToProps)(Pagination);
