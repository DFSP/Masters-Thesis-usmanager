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

import React from 'react';
import Sidenav from "../sidenav/Sidenav";
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";
import Breadcrumbs from "../../components/breadcrumbs/Breadcrumbs";
import M from "materialize-css";
import {RouteComponentProps, withRouter} from "react-router";
import {bindActionCreators} from "redux";
import {updateSearch} from "../../actions";
import ReactTooltip from "react-tooltip";

interface StateToProps {
    sidenavVisible: boolean;
}

interface DispatchToProps {
    updateSearch: (search: string) => void;
}

interface IMainLayout {
    children?: React.ReactNode;
}

type Props = IMainLayout & StateToProps & DispatchToProps & RouteComponentProps;

class MainLayout extends React.Component<Props, {}> {

    public componentDidMount(): void {
        M.AutoInit();
        this.props.updateSearch('');
        window.scrollTo(0, 0)
    }

    public render() {
        const paddingLeft = this.props.sidenavVisible ? 225 : 0;
        return (
            <div>
                <Sidenav/>
                <ReactTooltip id='tooltip' effect='solid' type='light'/>
                <ReactTooltip id='dark-tooltip' effect='solid' type='dark'/>
                <div className="section content" style={{paddingLeft, transition: 'padding-left .25s'}}>
                    <div className="row col s12">
                        {/* @ts-ignore*/}
                        <Breadcrumbs/>
                    </div>
                    <div className='row col s12 m12'>
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
    }
);

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({updateSearch}, dispatch);

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainLayout));
