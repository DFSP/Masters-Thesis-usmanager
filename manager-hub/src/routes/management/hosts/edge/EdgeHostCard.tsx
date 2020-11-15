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

import Card from "../../../../components/cards/Card";
import CardItem from "../../../../components/list/CardItem";
import React from "react";
import {IEdgeHost} from "./EdgeHost";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {deleteEdgeHost} from "../../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface EdgeHostCardProps {
    edgeHost: IEdgeHost;
}

interface DispatchToProps {
    deleteEdgeHost: (edgeHost: IEdgeHost) => void;
}

type Props = DispatchToProps & EdgeHostCardProps;

class EdgeHostCard extends BaseComponent<Props, State> {

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

    private onDeleteSuccess = (edgeHost: IEdgeHost): void => {
        super.toast(`<span class="green-text">Edge host <b class="white-text">${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b> foi removido com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteEdgeHost(edgeHost);
    }

    private onDeleteFailure = (reason: string, edgeHost: IEdgeHost): void => {
        super.toast(`Não foi possível remover o edge host <a href='/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}'><b>${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {edgeHost} = this.props;
        const publicIpAddress = edgeHost.publicIpAddress;
        const privateIpAddress = edgeHost.privateIpAddress;
        return [
            <LinkedContextMenuItem
                option={'Modificar a lista de regras'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'rules'}
                state={edgeHost}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de regras genéricas'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'genericRules'}
                state={edgeHost}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista das métricas simuladas'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'simulatedMetrics'}
                state={edgeHost}/>,
            <LinkedContextMenuItem
                option={'Ver a lista das métricas simuladas genéricas'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'genericSimulatedMetrics'}
                state={edgeHost}/>,
            <LinkedContextMenuItem
                option={'Executar comandos'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'ssh'}
                state={edgeHost}/>,
            <LinkedContextMenuItem
                option={'Carregar ficheiros'}
                pathname={`/hosts/edge/${publicIpAddress}-${privateIpAddress}`}
                selected={'sftp'}
                state={edgeHost}/>
        ];
    }

    public render() {
        const {edgeHost} = this.props;
        const {loading} = this.state;
        const CardEdgeHost = Card<IEdgeHost>();
        return <CardEdgeHost id={`edgeHost-${edgeHost.id}`}
                             title={edgeHost.publicIpAddress + '/' + edgeHost.privateIpAddress}
                             link={{
                                 to: {
                                     pathname: `/hosts/edge/${edgeHost.publicIpAddress}-${edgeHost.privateIpAddress}`,
                                     state: edgeHost
                                 }
                             }}
                             height={'213px'}
                             margin={'10px 0'}
                             hoverable
                             delete={{
                                 confirmMessage: `apagar host ${edgeHost.publicIpAddress}`,
                                 url: `hosts/edge/${edgeHost.publicIpAddress}/${edgeHost.privateIpAddress}`,
                                 successCallback: this.onDeleteSuccess,
                                 failureCallback: this.onDeleteFailure,
                             }}
                             loading={loading}
                             bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'username'}
                      label={'Username'}
                      value={`${edgeHost.username}`}/>
            <CardItem key={'publicIpAddress'}
                      label={'Public ip address'}
                      value={`${edgeHost.publicIpAddress}`}/>
            <CardItem key={'privateIpAddress'}
                      label={'Private ip address'}
                      value={`${edgeHost.privateIpAddress}`}/>
            <CardItem key={'publicDnsName'}
                      label={'Public dns name'}
                      value={`${edgeHost.publicDnsName}`}/>
            <CardItem key={'region'}
                      label={'Region'}
                      value={`${edgeHost.region.region}`}/>
            <CardItem key={'coordinates'}
                      label={'Coordinates'}
                      value={`(${edgeHost.coordinates.latitude.toFixed(3)}, ${edgeHost.coordinates.longitude.toFixed(3)})`}/>
            {edgeHost.managedByWorker &&
            <CardItem key={'managedByWorker'}
                      label={'managedByWorker'}
                      value={`${edgeHost.managedByWorker.id}`}/>}
        </CardEdgeHost>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteEdgeHost,
};

export default connect(null, mapDispatchToProps)(EdgeHostCard);
