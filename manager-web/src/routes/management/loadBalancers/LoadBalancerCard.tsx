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
import {deleteContainer} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface LoadBalancerCardProps {
    loadBalancer: ILoadBalancer;
}

interface DispatchToProps {
    deleteContainer: (loadBalancer: ILoadBalancer) => void;
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
        super.toast(`<span class="green-text">Load-balancer <b class="white-text">${loadBalancer.containerId}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteContainer(loadBalancer)
    }

    private onStopFailure = (reason: string, loadBalancer: ILoadBalancer): void => {
        super.toast(`Unable to stop <a href=/load-balancers/${loadBalancer.containerId}><b>${loadBalancer.containerId}</b></a> load-balancer`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {loadBalancer} = this.props;
        return [
            <LinkedContextMenuItem
                option={'View ports'}
                pathname={`/containers/${loadBalancer.containerId}#ports`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'View labels'}
                pathname={`/containers/${loadBalancer.containerId}#labels`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Check logs'}
                pathname={`/containers/${loadBalancer.containerId}#logs`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Modify rules'}
                pathname={`/containers/${loadBalancer.containerId}#rules`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'View generic rules'}
                pathname={`/containers/${loadBalancer.containerId}#genericContainerRules`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'Modify simulated metrics'}
                pathname={`/containers/${loadBalancer.containerId}#simulatedMetrics`}
                state={loadBalancer}/>,
            <LinkedContextMenuItem
                option={'View generic simulated metrics'}
                pathname={`/containers/${loadBalancer.containerId}#genericSimulatedMetrics`}
                state={loadBalancer}/>
        ];
    }

    public render() {
        const {loadBalancer} = this.props;
        const {loading} = this.state;
        const CardLoadBalancer = Card<IContainer>();
        return <CardLoadBalancer id={`load-balancer-${loadBalancer.id}`}
                                 title={loadBalancer.containerId.toString()}
                                 link={{to: {pathname: `/load-balancers/${loadBalancer.containerId}`, state: loadBalancer}}}
                                 height={'125px'}
                                 margin={'10px 0'}
                                 hoverable
                                 delete={{
                                     textButton: 'Stop',
                                     url: `containers/${loadBalancer.containerId}`,
                                     successCallback: this.onStopSuccess,
                                     failureCallback: this.onStopFailure,
                                 }}
                                 loading={loading}
                                 contextMenuItems={this.contextMenu()}>
            <CardItem key={'hostname'}
                      label={'Hostname'}
                      value={loadBalancer.publicIpAddress}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${loadBalancer.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
        </CardLoadBalancer>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteContainer
};

export default connect(null, mapDispatchToProps)(LoadBalancerCard);