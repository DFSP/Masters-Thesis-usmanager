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
import {awsInstanceStates, ICloudHost} from "./CloudHost";
import BaseComponent from "../../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../../components/contextmenu/LinkedContextMenuItem";
import {deleteData, IReply, postData} from "../../../../utils/api";
import ActionContextMenuItem from "../../../../components/contextmenu/ActionContextMenuItem";
import DividerContextMenuItem from "../../../../components/contextmenu/DividerContextMenuItem";
import {deleteCloudHost, updateCloudHost} from "../../../../actions";
import {connect} from "react-redux";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import {RouteComponentProps, withRouter} from "react-router";

interface State {
    loading: boolean;
    cloudHost?: ICloudHost,
}

interface CloudHostCardProps {
    cloudHost: ICloudHost;
}

interface DispatchToProps {
    updateCloudHost: (previousCloudHost: ICloudHost, currentCloudHost: ICloudHost) => void;
    deleteCloudHost: (cloudHost: ICloudHost) => void;
}

type Props = DispatchToProps & CloudHostCardProps & RouteComponentProps;

class CloudHostCard extends BaseComponent<Props, State> {

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

    private getCloudHost = () =>
        this.state.cloudHost || this.props.cloudHost;

    private startCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}/state`;
        this.setState({loading: true});
        postData(url, 'start',
            (reply: IReply<ICloudHost>) => this.onStartSuccess(reply.data),
            (reason) => this.onStartFailure(reason, cloudHost));
    };

    private onStartSuccess = (cloudHost: ICloudHost) => {
        super.toast(`<span class="green-text">Successfully started ${this.mounted ? `<b class="white-text">${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance</span>`, 15000);
        const previousCloudHost = this.getCloudHost();
        if (previousCloudHost?.id) {
            this.props.updateCloudHost(previousCloudHost as ICloudHost, cloudHost)
        }
        if (this.mounted) {
            this.updateCloudHost(cloudHost);
        }
    };

    private onStartFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to start ${this.mounted ? `<b>${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private stopCloudHost = () => {
         const cloudHost = this.getCloudHost();
         const url = `hosts/cloud/${cloudHost.instanceId}/state`;
         this.setState({loading: true});
         postData(url, 'stop',
             (reply: IReply<ICloudHost>) => this.onStopSuccess(reply.data),
             (reason) => this.onStopFailure(reason, cloudHost));
    };

    private onStopSuccess = (cloudHost: ICloudHost) => {
        super.toast(`<span class="green-text">Successfully stopped ${this.mounted ? `<b class="white-text">${cloudHost.instanceId}</b>` : `<a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a>`} instance</span>`, 15000);
        const previousCloudHost = this.getCloudHost();
        if (previousCloudHost?.id) {
            this.props.updateCloudHost(previousCloudHost as ICloudHost, cloudHost)
        }
        if (this.mounted) {
            this.updateCloudHost(cloudHost);
        }
    };

    private onStopFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to stop <a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a> instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private terminateCloudHost = () => {
        const cloudHost = this.getCloudHost();
        const url = `hosts/cloud/${cloudHost.instanceId}`;
        this.setState({loading: true});
        deleteData(url,
            () => this.onTerminateSuccess(cloudHost),
            (reason) => this.onTerminateFailure(reason, cloudHost));
    };

    private onTerminateSuccess = (cloudHost: ICloudHost) => {
        super.toast(`<span class="green-text">Successfully terminated <b class="white-text">${cloudHost.instanceId}</b> instance</span>`, 15000);
        if (this.mounted) {
            this.props.deleteCloudHost(cloudHost);
        }
    };

    private onTerminateFailure = (reason: string, cloudHost: Partial<ICloudHost>) => {
        super.toast(`Failed to terminate <a href=/hosts/cloud/${cloudHost.instanceId}><b>${cloudHost.instanceId}</b></a> instance`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private updateCloudHost = (cloudHost: ICloudHost) => {
        cloudHost = Object.values(normalize(cloudHost, Schemas.CLOUD_HOST).entities.cloudHosts || {})[0];
        this.setState({cloudHost: cloudHost, loading: false});
    };

    private topContextMenu = (): JSX.Element[] => {
        const cloudHost = this.getCloudHost();
        const menus = [];
        if (cloudHost.state.name === awsInstanceStates.STOPPED.name) {
            menus.push(<ActionContextMenuItem className='green-text' option='Start' state={cloudHost} onClick={this.startCloudHost}/>);
        }
        if (cloudHost.state.name === awsInstanceStates.RUNNING.name) {
            menus.push(<ActionContextMenuItem className='blue-text' option='Stop' state={cloudHost} onClick={this.stopCloudHost}/>);
        }
        if (!cloudHost.state.name.includes(awsInstanceStates.TERMINATED.name)
            && !cloudHost.state.name.includes(awsInstanceStates.SHUTTING_DOWN.name)) {
            menus.push(<ActionContextMenuItem className='red-text' option='Terminate' state={cloudHost} onClick={this.terminateCloudHost}/>);
        }
        return menus;
    }

    private bottomContextMenu = (): JSX.Element[] => {
        const cloudHost = this.getCloudHost();
        const id = cloudHost.publicIpAddress || cloudHost.instanceId;
        return [
            <LinkedContextMenuItem
                option={'Modify rules'}
                pathname={`/hosts/cloud/${id}`}
                selected={'rules'}
                state={cloudHost}/>,
            <LinkedContextMenuItem
                option={'View generic rules'}
                pathname={`/hosts/cloud/${id}`}
                selected={'genericContainerRules'}
                state={cloudHost}/>,
            <LinkedContextMenuItem
                option={'Modify simulated metrics'}
                pathname={`/hosts/cloud/${id}`}
                selected={'simulatedMetrics'}
                state={cloudHost}/>,
            <LinkedContextMenuItem
                option={'View generic simulated metrics'}
                pathname={`/hosts/cloud/${id}`}
                selected={'genericSimulatedMetrics'}
                state={cloudHost}/>
        ];
    }

    public render() {
        const cloudHost = this.getCloudHost();
        const {loading} = this.state;
        const CardCloudHost = Card<ICloudHost>();
        return <CardCloudHost id={`cloudHost-${cloudHost.publicIpAddress || cloudHost.instanceId}`}
                              title={cloudHost.publicIpAddress || cloudHost.instanceId}
                              link={{
                                  to: {
                                      pathname: `/hosts/cloud/${cloudHost.instanceId}`,
                                      state: cloudHost
                                  }
                              }}
                              height={'215px'}
                              margin={'10px 0'}
                              hoverable
                              loading={loading}
                              topContextMenuItems={this.topContextMenu()}
                              bottomContextMenuItems={this.bottomContextMenu()}>
            <CardItem key={'imageId'}
                      label={'Image id'}
                      value={`${cloudHost.imageId}`}/>
            <CardItem key={'instanceType'}
                      label={'Instance type'}
                      value={`${cloudHost.instanceType}`}/>
            <CardItem key={'state'}
                      label={'State'}
                      value={`${cloudHost.state.name}`}/>
            {cloudHost.publicDnsName &&
            <CardItem key={'publicDnsName'}
                      label={'Public dns name'}
                      value={`${cloudHost.publicDnsName}`}/>}
            {cloudHost.publicIpAddress &&
            <CardItem key={'publicIpAddress'}
                      label={'Public ip address'}
                      value={`${cloudHost.publicIpAddress}`}/>}
            {cloudHost.privateIpAddress &&
            <CardItem key={'privateIpAddress'}
                      label={'Private ip address'}
                      value={`${cloudHost.privateIpAddress}`}/>}
            <CardItem key={'placement'}
                      label={'Placement'}
                      value={`${cloudHost.placement.availabilityZone}`}/>
            <CardItem key={'coordinates'}
                      label={'Coordinates'}
                      value={`(${cloudHost.region.coordinates.latitude.toFixed(3)}, ${cloudHost.region.coordinates.longitude.toFixed(3)})`}/>
            {cloudHost.managedByWorker &&
            <CardItem key={'managedByWorker'}
                      label={'Managed by worker'}
                      value={`${cloudHost.managedByWorker.id}`}/>}
        </CardCloudHost>;
    }

}

const mapDispatchToProps: DispatchToProps = {
    updateCloudHost,
    deleteCloudHost,
};

export default withRouter(connect(null, mapDispatchToProps)(CloudHostCard));