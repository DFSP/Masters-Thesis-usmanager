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
import CardItem from "../../../components/list/CardItem";

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
        super.toast(`<span class='green-text'>A aplicação <b class='white-text'>${app.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteApp(app);
    }

    private onDeleteFailure = (reason: string, app: IApp): void => {
        super.toast(`Não foi possível remover a aplicação <a href='/aplicações/${app.name}'><b>${app.name}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {app} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modificar a lista de serviços'}
                pathname={`/aplicações/${app.name}`}
                selected={'services'}
                state={app}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista de regras'}
                pathname={`/aplicações/${app.name}`}
                selected={'rules'}
                state={app}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de regras genéricas'}
                pathname={`/aplicações/${app.name}`}
                selected={'genericRules'}
                state={app}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista das métricas simuladas'}
                pathname={`/aplicações/${app.name}`}
                selected={'simulatedMetrics'}
                state={app}/>,
            <LinkedContextMenuItem
                option={'Ver a lista das métricas simuladas genéricas'}
                pathname={`/aplicações/${app.name}`}
                selected={'genericSimulatedMetrics'}
                state={app}/>
        ];
    }

    public render() {
        const {app} = this.props;
        const {loading} = this.state;
        const CardApp = Card<IApp>();
        let description = app.description || 'No description available';
        if (!description.endsWith('.')) {
            description += '.';
        }
        return <CardApp id={`app-${app.name}`}
                        title={app.name}
                        link={{to: {pathname: `/aplicações/${app.name}`, state: app}}}
                        height={'175px'}
                        margin={'10px 0'}
                        hoverable
                        delete={{
                            url: `apps/${app.name}`,
                            successCallback: this.onDeleteSuccess,
                            failureCallback: this.onDeleteFailure
                        }}
                        loading={loading}
                        bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'app'}
                      value={description}/>
        </CardApp>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteApp
};

export default connect(null, mapDispatchToProps)(AppCard);