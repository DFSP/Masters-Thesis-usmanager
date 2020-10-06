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

import Card from "../../../components/cards/Card";
import React from "react";
import {IApp} from "./App";
import Form from "../../../components/form/Form";
import BaseComponent from "../../../components/BaseComponent";
import {MenuItem, MenuItemProps} from "react-contextmenu";
import {Link} from "react-router-dom";

interface State {
    loading: boolean;
}

interface AppCardProps {
    app: IApp;
}

type Props = AppCardProps;

export default class AppCard extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            loading: false
        }
    }

    public componentDidMount(): void {
        this.mounted = true;
    };

    componentWillUnmount(): void {
        this.mounted = false;
    }

    private onDeleteSuccess = (app: IApp): void => {
        super.toast(`<span class="green-text">App <b class="white-text">${app.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private onDeleteFailure = (reason: string, app: IApp): void => {
        super.toast(`Unable to delete <b>${app.name}</b> app`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {app} = this.props;
        return [
            <Link to={{
                pathname: `/apps/${app.name}#services`,
                state: app
            }}>
                <MenuItem className='custom-context-menu-item'>
                    Modify services
                </MenuItem>
            </Link>
        ];
    }

    public render() {
        const {app} = this.props;
        const {loading} = this.state;
        const CardApp = Card<IApp>();
        return <CardApp id={`app-${app.name}`}
                        title={app.name}
                        link={{to: {pathname: `/apps/${app.name}#app`, state: app}}}
                        height={'30px'}
                        margin={'10px 0'}
                        hoverable
                        delete={{
                            url: `apps/${app.name}`,
                            successCallback: this.onDeleteSuccess,
                            failureCallback: this.onDeleteFailure
                        }}
                        loading={loading}
                        contextMenuItems={this.contextMenu()}/>
    }

}
