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

import {Link} from "react-router-dom";
import React, {createRef} from "react";
import M from "materialize-css";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {showSidenavByUser, showSidenavByWidth} from "../../actions";
import {ReduxState} from "../../reducers";
import ScrollBar from "react-perfect-scrollbar";
import {IComponent} from "../../containers/Root.dev";

type ILink = { link: string, name: string, sub?: ILink[] }

const sidenavManagementLinks: ILink[] = [
    {link: '/apps', name: 'Apps'},
    {link: '/services', name: 'Services'},
    {link: '/containers', name: 'Containers'},
    {
        link: '/hosts', name: 'Hosts', sub: [
            {link: '/cloud', name: 'Cloud instances'},
            {link: '/edge', name: 'Edge hosts'}
        ]
    },
    {link: '/nodes', name: 'Nodes'},
    {link: '/regions', name: 'Regions'},
    {
        link: '/rules', name: 'Rules', sub: [
            {link: '/conditions', name: 'Conditions'},
            {link: '/hosts', name: 'Host rules'},
            {link: '/apps', name: 'App rules'},
            {link: '/services', name: 'Service rules'},
            {link: '/containers', name: 'Container rules'},
        ]
    },
    {
        link: '/simulated-metrics', name: 'Simulated metrics', sub: [
            {link: '/hosts', name: 'Host metrics'},
            {link: '/apps', name: 'App metrics'},
            {link: '/services', name: 'Service metrics'},
            {link: '/containers', name: 'Container metrics'}
        ]
    },
    {link: '/load-balancers', name: 'Load balancers'},
    {link: '/eureka-servers', name: 'Eureka servers'},
    {link: '/worker-managers', name: 'Worker managers'},
    {link: '/ssh', name: 'Ssh'},
    {link: '/settings', name: 'Settings'},
    {link: '/logs', name: 'Logs'},
];

const sidenavMonitoringLinks: ILink[] = [
    {link: '/settings', name: 'Settings'},
];

const sidenavDataManagementLinks: ILink[] = [
    {link: '/settings', name: 'Settings'},
];

interface StateToProps {
    sidenav: { user: boolean, width: boolean },
    component: IComponent,
}

interface DispatchToProps {
    showSidenavByUser: (value: boolean) => void;
    showSidenavByWidth: (value: boolean) => void;
}

type Props = StateToProps & DispatchToProps;

class Sidenav extends React.Component<Props, {}> {

    private sidenav = createRef<HTMLUListElement>();
    private scrollbar: (ScrollBar | null) = null;

    public componentDidMount(): void {
        window.addEventListener('resize', this.handleResize);
        M.Sidenav.init(this.sidenav.current as Element);
        this.scrollbar?.updateScroll();
        this.handleResize();
        this.blockBodyScroll();
    };

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.scrollbar?.updateScroll();
    }

    public componentWillUnmount(): void {
        window.removeEventListener('resize', this.handleResize);
    }

    public render() {
        let links = (function (component) {
            switch (component) {
                case "Management":
                    return sidenavManagementLinks;
                case "Monitoring":
                    return sidenavMonitoringLinks;
                case "Data":
                    return sidenavDataManagementLinks;
            }
        })(this.props.component);
        return (
            <ul id="slide-out" className="sidenav sidenav-fixed no-shadows"
                style={this.props.sidenav.user ? {width: 200, transition: 'width .25s'} : {
                    width: 0,
                    transition: 'width .25s'
                }} ref={this.sidenav}>
                <div className="sidenav-menu">
                    <a
                        className="sidenav-icon sidenav-trigger transparent btn-floating btn-flat btn-small waves-effect waves-light"
                        data-target="slide-out"
                        onClick={this.handleSidenav}>
                        <i className="material-icons">menu</i>
                    </a>
                </div>
                <ScrollBar ref={(ref) => {
                    this.scrollbar = ref;
                }}
                           component="div">
                    {links.map((link, index) =>
                        <div key={index}>
                            <li>
                                <Link className="white-text" to={link.link} onClick={this.closeSlideSidenav}>
                                    <span style={{whiteSpace: 'nowrap'}}>{link.name}</span>
                                </Link>
                            </li>
                            {link.sub && link.sub.map((sublink, index) =>
                                <div key={index}>
                                    {<li>
                                        <div className="divider grey darken-4"/>
                                    </li>}
                                    <li>
                                        <Link className="white-text sub-link" to={`${link.link}${sublink.link}`}
                                              onClick={this.closeSlideSidenav}>
                                            <span style={{whiteSpace: 'nowrap'}}>{sublink.name}</span>
                                        </Link>
                                    </li>
                                </div>
                            )}
                            {index < sidenavManagementLinks.length - 1 && <li>
                                <div className="divider grey darken-3"/>
                            </li>}
                        </div>
                    )}
                </ScrollBar>
            </ul>
        )
    }

    private blockBodyScroll = () => {
        this.sidenav.current?.addEventListener('wheel', event => event.preventDefault())
    };

    private shouldShowSidenav = () =>
        window.innerWidth > 992;

    private handleResize = () => {
        let show = this.shouldShowSidenav();
        const {width} = this.props.sidenav;
        if (show !== width) {
            this.props.showSidenavByWidth(show);
        }
        let sidenav = M.Sidenav.getInstance(this.sidenav.current as Element);
        let {isOpen} = sidenav;
        if (isOpen && !width && window.innerWidth > 992) {
            sidenav.close();
        }
        this.scrollbar?.updateScroll();
    };

    private handleSidenav = () => {
        let sidenav = M.Sidenav.getInstance(this.sidenav.current as Element);
        let {isOpen} = sidenav;
        this.props.showSidenavByUser(!isOpen);
    };

    private closeSlideSidenav = () => {
        let sidenav = M.Sidenav.getInstance(this.sidenav.current as Element);
        const {width} = this.props.sidenav;
        if (!width) {
            sidenav.close();
        }
    };
}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenav: state.ui.sidenav,
        component: state.ui.component
    }
);

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({showSidenavByUser, showSidenavByWidth}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(Sidenav);
