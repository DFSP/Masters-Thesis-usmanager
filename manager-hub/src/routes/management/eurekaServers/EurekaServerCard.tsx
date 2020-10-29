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
import {IEurekaServer} from "./EurekaServer";
import {IContainer} from "../containers/Container";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {deleteContainer} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface EurekaServerCardProps {
    eurekaServer: IEurekaServer;
}

interface DispatchToProps {
    deleteContainer: (eurekaServer: IEurekaServer) => void;
}

type Props = DispatchToProps & EurekaServerCardProps;

class EurekaServerCard extends BaseComponent<Props, State> {

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

    private onStopSuccess = (eurekaServer: IEurekaServer): void => {
        super.toast(`<span class="green-text">Eureka server <b class="white-text">${eurekaServer.containerId}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteContainer(eurekaServer)
    }

    private onStopFailure = (reason: string, eurekaServer: IEurekaServer): void => {
        super.toast(`Unable to stop eureka-server <a href=/eureka-servers/${eurekaServer.containerId}><b>${eurekaServer.containerId}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {eurekaServer} = this.props;
        return [
            <LinkedContextMenuItem
                option={'View ports'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'ports'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'View labels'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'labels'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'Check logs'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'logs'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'Modify rules'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'rules'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'View generic rules'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'genericContainerRules'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'Modify simulated metrics'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'simulatedMetrics'}
                state={eurekaServer}/>,
            <LinkedContextMenuItem
                option={'View generic simulated metrics'}
                pathname={`/containers/${eurekaServer.containerId}`}
                selected={'genericSimulatedMetrics'}
                state={eurekaServer}/>
        ];
    }

    public render() {
        const {eurekaServer} = this.props;
        const {loading} = this.state;
        const CardEurekaServer = Card<IContainer>();
        return <CardEurekaServer id={`eurekaServer-${eurekaServer.containerId}`}
                                 title={eurekaServer.containerId.toString()}
                                 link={{
                                     to: {
                                         pathname: `/eureka-servers/${eurekaServer.containerId}`,
                                         state: eurekaServer
                                     }
                                 }}
                                 height={'85px'}
                                 margin={'10px 0'}
                                 hoverable
                                 delete={{
                                     textButton: 'Stop',
                                     url: `containers/${eurekaServer.containerId}`,
                                     successCallback: this.onStopSuccess,
                                     failureCallback: this.onStopFailure,
                                 }}
                                 loading={loading}
                                 bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'host'}
                      label={'Host'}
                      value={eurekaServer.publicIpAddress}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${eurekaServer.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
        </CardEurekaServer>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteContainer
};

export default connect(null, mapDispatchToProps)(EurekaServerCard);