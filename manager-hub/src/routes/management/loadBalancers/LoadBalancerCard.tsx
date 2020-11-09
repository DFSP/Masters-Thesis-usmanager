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
import CardItem from "../../../components/list/CardItem";
import Card from "../../../components/cards/Card";
import {ILoadBalancer} from "./LoadBalancer";
import {IContainer} from "../containers/Container";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {deleteContainer, deleteLoadBalancer} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface LoadBalancerCardProps {
    loadBalancer: ILoadBalancer;
}

interface DispatchToProps {
    deleteLoadBalancer: (loadBalancer: ILoadBalancer) => void;
}

type Props = DispatchToProps & LoadBalancerCardProps;

class LoadBalancerCard extends BaseComponent<Props, State> {

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

    private onStopSuccess = (loadBalancer: ILoadBalancer): void => {
        super.toast(`<span class="green-text">O balanceador de carga <b class="white-text">${loadBalancer.containerId}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteLoadBalancer(loadBalancer) //TODO
    }

    private onStopFailure = (reason: string, loadBalancer: ILoadBalancer): void => {
        super.toast(`Não foi possível para a instância do balanceamento de carga<a href='/balanceamento de carga/${loadBalancer.containerId}'><b>${loadBalancer.containerId}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {loadBalancer} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Ver as portas associadas'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'ports'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Ver as labels associadas'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'labels'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Ver as logs'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'logs'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista de regras'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'rules'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de regras genéricas'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'genericContainerRules'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista das métricas simuladas'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'simulatedMetrics'}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Ver a lista das métricas simuladas genéricas'}
                pathname={`/contentores/${loadBalancer.containerId}`}
                selected={'genericSimulatedMetrics'}
                state={loadBalancer}/>
        ];
    }

    public render() {
        const {loadBalancer} = this.props;
        const {loading} = this.state;
        const CardLoadBalancer = Card<IContainer>();
        return <CardLoadBalancer id={`load-balancer-${loadBalancer.id}`}
                                 title={loadBalancer.containerId.toString()}
                                 link={{
                                     to: {
                                         pathname: `/balanceamento de carga/${loadBalancer.containerId}`,
                                         state: loadBalancer
                                     }
                                 }}
                                 height={'85px'}
                                 margin={'10px 0'}
                                 hoverable
                                 delete={{
                                     textButton: 'Parar',
                                     url: `containers/${loadBalancer.containerId}`,
                                     successCallback: this.onStopSuccess,
                                     failureCallback: this.onStopFailure,
                                 }}
                                 loading={loading}
                                 bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'host'}
                      label={'Host'}
                      value={loadBalancer.publicIpAddress}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${loadBalancer.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
        </CardLoadBalancer>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteLoadBalancer
};

export default connect(null, mapDispatchToProps)(LoadBalancerCard);