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
import {Link, RouteComponentProps, withRouter} from "react-router-dom";
import {FaDocker, GoMarkGithub} from "react-icons/all";
import SearchBar from "./SearchBar";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {changeComponent, showSidenavByUser} from "../../actions";
import './Navbar.css';
import {ReduxState} from "../../reducers";
import {components, IComponent, managementAuthenticatedRoutes} from "../../containers/Root.dev";
import {getLoggedInUser, logout} from "../../utils/auth";
import {Dropdown} from "../../components/form/Dropdown";

const logo = require("../../resources/images/logo.png");

interface StateToProps {
    sidenav: { user: boolean, width: boolean },
    component: IComponent,
}

interface DispatchToProps {
    showSidenavByUser: (value: boolean) => void;
    changeComponent: (component: IComponent) => void;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps;

interface State {
    animate: boolean;
}

class Navbar extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {
            animate: false,
        }
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        const prevSidenavVisibility = prevProps.sidenav.user && prevProps.sidenav.width;
        const currentSidenavVisibility = this.props.sidenav.user && this.props.sidenav.width;
        const prevLoggingIn = this.loggingIn(prevProps.location.pathname);
        const loggingIn = this.loggingIn(this.props.location.pathname);
        if (this.state.animate && prevLoggingIn && !loggingIn) {
            // coming from login, dont animate
            this.setState({animate: false});
        }
        if (!this.state.animate && prevSidenavVisibility !== currentSidenavVisibility) {
            this.setState({animate: true});
        }
    }

    public render() {
        const {pathname} = this.props.location;
        const {user: sidenavUser, width: sidenavWidth} = this.props.sidenav;
        const loggingIn = this.loggingIn(pathname);
        const showSidenav = sidenavUser && sidenavWidth;
        const route = managementAuthenticatedRoutes[pathname];
        const showSearchbar = route && managementAuthenticatedRoutes[pathname].search;
        let loggedInUser = getLoggedInUser();
        let logoStyle;
        if (showSidenav && !loggingIn) {
            logoStyle = {
                paddingLeft: '225px', marginLeft: '-74px', marginRight: "24px"
            };
            if (this.state.animate) {
                logoStyle = {...logoStyle, transition: "padding-left .25s, margin-left .25s"}
            }
        } else {
            logoStyle = {
                marginRight: "0"
            }
            if (this.state.animate) {
                logoStyle = {...logoStyle, transition: "padding-left .25s margin-right .25s"}
            }
        }
        return (
            <header role="navigation">
                <div className="navbar-fixed">
                    <nav className="no-shadows">
                        <div className="nav-wrapper row">
                            {!loggingIn &&
                            <a className="sidenav-trigger transparent btn-floating btn-flat btn-small"
                               data-target="slide-out"
                               onClick={this.handleSidenav}>
                                <i className="material-icons">menu</i>
                            </a>}
                            <ul className="left">
                                <li style={logoStyle} className={'hide-on-med-and-down'}>
                                    <Link className="transparent brand-logo" to={"/gestor"}>
                                        <img src={logo} alt=""/>
                                        Manager hub
                                    </Link>
                                </li>
                            </ul>
                            {showSearchbar && <SearchBar/>}
                            <ul className="right">
                                {!loggingIn && <li className="components">
                                    <Dropdown<IComponent>
                                        id={'components'}
                                        name={'components'}
                                        value={this.props.component}
                                        onChange={this.changeComponent}
                                        dropdown={{
                                            defaultValue: 'Componente',
                                            values: components,
                                            optionToString: this.componentOption
                                        }}>
                                    </Dropdown>
                                </li>}
                                <li className="username">
                                    {loggedInUser}
                                </li>
                                <li>
                                    <a data-for='tooltip' data-tip="GitHub" data-place='bottom'
                                       href="https://github.com/usmanager/" target="_blank" rel="noopener noreferrer">
                                        <i className="material-icons"><GoMarkGithub/></i>
                                    </a>
                                </li>
                                <li>
                                    <a data-for='tooltip' data-tip="Docker Hub" data-place='bottom'
                                       href="https://hub.docker.com/orgs/usmanager" target="_blank"
                                       rel="noopener noreferrer">
                                        <i className="material-icons"><FaDocker/></i>
                                    </a>
                                </li>
                                {!loggingIn && <li>
                                    <a className="red-text text-darken-4" onClick={this.handleLogout}>
                                        <i className="material-icons right">logout</i> Sair
                                    </a>
                                </li>}
                            </ul>
                        </div>
                    </nav>
                </div>
            </header>
        )
    }

    private handleSidenav = () => {
        let {user, width} = this.props.sidenav;
        user = !width || !user;
        this.props.showSidenavByUser(user);
    };

    private handleLogout = () => {
        logout();
        this.setState({animate: true}, () => this.props.history.push("/"));
    };

    private loggingIn = (pathname: string): boolean =>
        pathname === '/' || pathname === '/login';

    private componentOption = (option: IComponent): string =>
        option.toString();

    private changeComponent = (e: React.FormEvent<HTMLSelectElement>) => {
        const selectedComponent = e.currentTarget.value as IComponent;
        this.props.changeComponent(selectedComponent);
        this.props.history.push('/gestor');
    }
}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenav: state.ui.sidenav,
        component: state.ui.component
    }
);

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({showSidenavByUser, changeComponent}, dispatch);

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Navbar));