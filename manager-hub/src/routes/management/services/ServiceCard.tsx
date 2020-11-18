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
        super.toast(`<span class="green-text">O serviço <b class="white-text">${service.serviceName}</b> foi removido com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteService(service);
    }

    private onDeleteFailure = (reason: string, service: IService): void => {
        super.toast(`Não foi possível remover <a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a> service`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {service} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Modificar a lista de aplicações'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'apps'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Alterar as dependências'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'dependencies'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de serviços dependentes'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'dependents'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Adicionar ou remover previsões'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'predictions'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista de regras'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'serviceRules'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de regras genéricas'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'genericRules'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista das métricas simuladas'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'simulatedMetrics'}
                state={service}/>,
            <LinkedContextMenuItem
                option={'Ver a lista das métricas simuladas genéricas'}
                pathname={`/serviços/${service.serviceName}`}
                selected={'genericSimulatedMetrics'}
                state={service}/>
        ];
    }

    private getReplicasMessage = (minimumReplicas: number, maximumReplicas: number): string | null => {
        if (minimumReplicas === maximumReplicas) {
            return minimumReplicas > 0 ? `${minimumReplicas}` : null;
        } else if (maximumReplicas === 0 || maximumReplicas == null) {
            return `At least ${minimumReplicas}`
        } else {
            return `At least ${minimumReplicas} up to ${maximumReplicas}`;
        }
    };

    public render() {
        const {service} = this.props;
        const {loading} = this.state;
        const CardService = Card<IService>();
        const replicasMessage = this.getReplicasMessage(service.minimumReplicas, service.maximumReplicas);
        return <CardService id={`service-${service.id}`}
                            title={service.serviceName}
                            link={{to: {pathname: `/serviços/${service.serviceName}`, state: service}}}
                            height={'200px'}
                            margin={'10px 0'}
                            hoverable
                            delete={{
                                confirmMessage: `apagar serviço ${service.serviceName}`,
                                url: `services/${service.serviceName}`,
                                successCallback: this.onDeleteSuccess,
                                failureCallback: this.onDeleteFailure
                            }}
                            loading={loading}
                            bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'serviceType'}
                      label={'Service type'}
                      value={`${service.serviceType}`}/>
            {service.serviceType !== 'SYSTEM' && replicasMessage &&
            <CardItem key={'replicas'}
                      label={'Replicas'}
                      value={replicasMessage}/>}
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${service.defaultExternalPort}:${service.defaultInternalPort}`}/>
            {service.launchCommand &&
            <CardItem key={'launchCommand'}
                      label={'Launch command'}
                      value={service.launchCommand}/>}
            {service.outputLabel &&
            <CardItem key={'outputLabel'}
                      label={'Output label'}
                      value={`${service.outputLabel}`}/>}
            {service.defaultDb &&
            <CardItem key={'database'}
                      label={'Database'}
                      value={service.defaultDb}/>}
            <CardItem key={'memory'}
                      label={'Memory usage'}
                      value={service.expectedMemoryConsumption > 0 ? `${service.expectedMemoryConsumption} bytes` : 'Unknown'}/>
        </CardService>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteService
};

export default connect(null, mapDispatchToProps)(ServiceCard);
