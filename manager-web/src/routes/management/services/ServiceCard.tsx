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
import {IService} from "./Service";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {EntitiesAction} from "../../../reducers/entities";
import {deleteService} from "../../../actions";
import {connect} from "react-redux";
import CardItem from "../../../components/list/CardItem";

interface State {
    loading: boolean;
}

interface ServiceCardProps {
    service: IService;
}

interface DispatchToProps {
    deleteService: (service: IService) => EntitiesAction;
}

type Props = DispatchToProps & ServiceCardProps;

class ServiceCard extends BaseComponent<Props, State> {

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

    private onDeleteSuccess = (service: IService): void => {
        super.toast(`<span class="green-text">Service <b class="white-text">${service.serviceName}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteService(service);
    }

    private onDeleteFailure = (reason: string, service: IService): void => {
        super.toast(`Unable to delete <a href=/services/${service.serviceName}><b>${service.serviceName}</b></a> service`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {service} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modify apps'}
                pathname={`/services/${service.serviceName}#apps`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modify dependencies'}
                pathname={`/services/${service.serviceName}#dependencies`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'View dependents'}
                pathname={`/services/${service.serviceName}#dependents`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modify predictions'}
                pathname={`/services/${service.serviceName}#predictions`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modify rules'}
                pathname={`/services/${service.serviceName}#serviceRules`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'View generic rules'}
                pathname={`/services/${service.serviceName}#genericRules`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modify simulated metrics'}
                pathname={`/services/${service.serviceName}#simulatedMetrics`}
                state={service}/>,
            <LinkedContextMenuItem
                option={'View generic simulated metrics'}
                pathname={`/services/${service.serviceName}#genericSimulatedMetrics`}
                state={service}/>
        ];
    }

    private getReplicasMessage = (minReplicas: number, maxReplicas: number): string => {
        if (minReplicas === maxReplicas) {
            return `${minReplicas}`;
        } else if (maxReplicas === 0) {
            return `At least ${minReplicas}`
        } else {
            return `At least ${minReplicas} up to ${maxReplicas}`;
        }
    };

    public render() {
        const {service} = this.props;
        const {loading} = this.state;
        const CardService = Card<IService>();
        return <CardService id={`service-${service.id}`}
                            title={service.serviceName}
                            link={{to: {pathname: `/services/${service.serviceName}`, state: service}}}
                            height={'250px'}
                            margin={'10px 0'}
                            hoverable
                            delete={{
                                url: `services/${service.serviceName}`,
                                successCallback: this.onDeleteSuccess,
                                failureCallback: this.onDeleteFailure
                            }}
                            loading={loading}
                            contextMenuItems={this.contextMenu()}>
            <CardItem key={'serviceType'}
                      label={'Service type'}
                      value={`${service.serviceType}`}/>
            <CardItem key={'replicas'}
                      label={'Replicas'}
                      value={this.getReplicasMessage(service.minReplicas, service.maxReplicas)}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${service.defaultExternalPort}:${service.defaultInternalPort}`}/>
            {service.launchCommand !== '' &&
            <CardItem key={'launchCommand'}
                      label={'Launch command'}
                      value={service.launchCommand}/>}
            <CardItem key={'outputLabel'}
                      label={'Output label'}
                      value={`${service.outputLabel}`}/>
            {service.defaultDb !== 'NOT_APPLICABLE' &&
            <CardItem key={'database'}
                      label={'Database'}
                      value={service.defaultDb}/>}
            <CardItem key={'memory'}
                      label={'Memory'}
                      value={`${service.expectedMemoryConsumption} bytes`}/>
        </CardService>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteService
};

export default connect(null, mapDispatchToProps)(ServiceCard);
