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
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {EntitiesAction} from "../../../reducers/entities";
import {deleteApp} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface AppCardProps {
    app: IApp;
}

interface DispatchToProps {
    deleteApp: (app: IApp) => EntitiesAction;
}

type Props = DispatchToProps & AppCardProps;

class AppCard extends BaseComponent<Props, State> {

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

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    private onDeleteSuccess = (app: IApp): void => {
        super.toast(`<span class="green-text">App <b class="white-text">${app.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteApp(app);
    }

    private onDeleteFailure = (reason: string, app: IApp): void => {
        super.toast(`Unable to delete <a href=/apps/${app.name}><b>${app.name}</b></a> app`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {app} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modify services'}
                pathname={`/apps/${app.name}`}
                selected={'services'}
                state={app}/>,
        ];
    }

    public render() {
        const {app} = this.props;
        const {loading} = this.state;
        const CardApp = Card<IApp>();
        return <CardApp id={`app-${app.name}`}
                        title={app.name}
                        link={{to: {pathname: `/apps/${app.name}`, state: app}}}
                        height={'30px'}
                        margin={'10px 0'}
                        hoverable
                        delete={{
                            url: `apps/${app.name}`,
                            successCallback: this.onDeleteSuccess,
                            failureCallback: this.onDeleteFailure
                        }}
                        loading={loading}
                        bottomContextMenuItems={this.contextMenu()}/>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteApp
};

export default connect(null, mapDispatchToProps)(AppCard);