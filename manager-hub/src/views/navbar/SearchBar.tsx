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

import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {updateSearch} from "../../actions";
import {ReduxState} from "../../reducers";
import styles from './SearchBar.module.css';

interface StateToProps {
    search: string;
}

interface DispatchToProps {
    updateSearch: (search: string) => void;
}

type Props = DispatchToProps & StateToProps;

class SearchBar extends React.Component<Props, {}> {

    public render() {
        return (
            <form className={`col l2 xl3 ${styles.searchBarForm}`} noValidate autoComplete="off">
                <div className={`input-field ${styles.searchBar}`}>
                    <input id="search" type="search" placeholder="Filter" value={this.props.search}
                           onChange={this.setValue}/>
                    <label className="label-icon" htmlFor="search">
                        <i className="material-icons">search</i>
                    </label>
                </div>
            </form>
        );
    }

    private setValue = ({target: {value}}: any) =>
        this.props.updateSearch(value);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        search: state.ui.search
    }
);

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({updateSearch}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(SearchBar);