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
import CardItem from "../../../components/list/CardItem";
import React from "react";
import {IContainer} from "./Container";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {addContainers, deleteContainer} from "../../../actions";
import {connect} from "react-redux";
import ContextSubMenuItem from "../../../components/contextmenu/ContextSubMenuItem";
import {INode} from "../nodes/Node";
import {IReply, postData} from "../../../utils/api";

interface State {
    loading: boolean;
    container?: IContainer,
}

interface ContainerCardProps {
    container: IContainer;
    nodes: { data: INode[], isLoading: boolean, error?: string | null },
}

interface DispatchToProps {
    deleteContainer: (container: IContainer) => void;
    addContainers: (containers: IContainer[]) => void;
}

type Props = DispatchToProps & ContainerCardProps;

class ContainerCard extends BaseComponent<Props, State> {

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

    private getContainer = () =>
        this.props.container || this.state.container;

    private onDeleteSuccess = (container: IContainer): void => {
        super.toast(`<span class='green-text'>O contentor <b class='white-text'>${container.id}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteContainer(container);
    }

    private onDeleteFailure = (reason: string, container: IContainer): void => {
        super.toast(`Erro ao parar o contentor <a href='/contentores/${container.id}'><b>${container.id}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private topContextMenu = (): JSX.Element[] => {
        const container = this.getContainer();
        const menus = [];
        if (container?.labels['serviceType'] !== 'SYSTEM') {
            menus.push(
                <ContextSubMenuItem<IContainer, INode> className={'blue-text'}
                                                       menu={'Replicate'}
                                                       state={container}
                                                       header={'Selecionar o endereço'}
                                                       emptyMessage={'Não há hosts disponíveis'}
                                                       submenus={Object.values(this.props.nodes.data)}
                                                       error={this.props.nodes.error}
                                                       menuToString={(option: INode) => `${option.publicIpAddress + (option.labels['privateIpAddress'] ? " (" + option.labels['privateIpAddress'] + ")" : '')}`}
                                                       onClick={this.migrate}/>,
                <ContextSubMenuItem<IContainer, INode> className={'blue-text'}
                                                       menu={'Migrate'}
                                                       state={container}
                                                       header={'Selecionar o endereço'}
                                                       emptyMessage={'Não há hosts disponíveis'}
                                                       submenus={Object.entries(this.props.nodes.data)
                                                           .filter(([_, node]) => node.publicIpAddress !== container.publicIpAddress && node.labels['privateIpAddress'] !== container.privateIpAddress)
                                                           .map(([_, node]) => node)}
                                                       error={this.props.nodes.error}
                                                       menuToString={(option: INode) => `${option.publicIpAddress + (option.labels['privateIpAddress'] ? " (" + option.labels['privateIpAddress'] + ")" : '')}`}
                                                       onClick={this.replicate}/>
            );
        }
        return menus;
    }

    private replicate = (event: React.MouseEvent, data: { state: IContainer, submenu: INode }) => {
        const container = data.state;
        const node = data.submenu;
        const publicIpAddress = node.publicIpAddress;
        const privateIpAddress = node.labels['privateIpAddress'];
        const url = `containers/${container?.id}/replicate`;
        this.setState({loading: true});
        postData(url, {publicIpAddress: publicIpAddress, privateIpAddress: privateIpAddress},
            (reply: IReply<IContainer>) => this.onReplicateSuccess(reply.data),
            (reason: string) => this.onReplicateFailure(reason, container));
    }

    private onReplicateSuccess = (container: IContainer) => {
        super.toast(`<span class='green-text'>O contentor ${container.image.split('/').splice(1)} foi replicado no contentor </span><a href='/contentores/${container.id}'><b>${container.id}</b></a>`, 15000);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.addContainers(Array(container));
    };

    private onReplicateFailure = (reason: string, container?: IContainer) => {
        super.toast(`Erro ao replicar o contentor ${this.mounted ? `<b>${container?.id}</b>` : `<a href='/contentores/${container?.id}'><b>${container?.id}</b></a>`}`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private migrate = (event: React.MouseEvent, data: { state: IContainer, submenu: INode }) => {
        const container = data.state;
        const node = data.submenu;
        const publicIpAddress = node.publicIpAddress;
        const privateIpAddress = node.labels['privateIpAddress'];
        const url = `containers/${container?.id}/migrate`;
        this.setState({loading: true});
        postData(url, {publicIpAddress: publicIpAddress, privateIpAddress: privateIpAddress},
            (reply: IReply<IContainer>) => this.onMigrateSuccess(reply.data),
            (reason) => this.onMigrateFailure(reason, container));
    }

    private onMigrateSuccess = (container: IContainer) => {
        const parentContainer = this.getContainer();
        super.toast(`<span class='green-text'>O contentor ${this.mounted ? parentContainer?.id : `<a href='/contentores/${parentContainer?.id}'>${parentContainer?.id}</a>`} foi migrado para o contentor </span><a href='/contentores/${container.id}'>${container.id}</a>`, 15000);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.addContainers(Array(container));
    };

    private onMigrateFailure = (reason: string, container?: IContainer) => {
        super.toast(`Erro ao migrar o contentor ${this.mounted ? `<b>${container?.id}</b>` : `<a href='/contentores/${container?.id}'><b>${container?.id}</b></a>`}`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    };

    private bottomContextMenu = (): JSX.Element[] => {
        const container = this.getContainer();
        return [
            <LinkedContextMenuItem
                option={'Ver as portas associadas'}
                pathname={`/contentores/${container.id}`}
                selected={'ports'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Ver as labels'}
                pathname={`/contentores/${container.id}`}
                selected={'containerLabels'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Ver as logs'}
                pathname={`/contentores/${container.id}`}
                selected={'logs'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista de regras'}
                pathname={`/contentores/${container.id}`}
                selected={'rules'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Ver a lista de regras genéricas'}
                pathname={`/contentores/${container.id}`}
                selected={'genericServiceRules'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Modificar a lista das métricas simuladas'}
                pathname={`/contentores/${container.id}`}
                selected={'simulatedMetrics'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Ver a lista das métricas simuladas genéricas'}
                pathname={`/contentores/${container.id}`}
                selected={'genericSimulatedMetrics'}
                state={container}/>
        ];
    }

    public render() {
        const container = this.getContainer();
        const {loading} = this.state;
        const CardContainer = Card<IContainer>();
        return <CardContainer id={`container-${container.id}`}
                              title={container.id.toString()}
                              link={{to: {pathname: `/contentores/${container.id}`, state: container}}}
                              height={'215px'}
                              margin={'10px 0'}
                              hoverable
                              delete={{
                                  textButton: 'Parar',
                                  confirmMessage: `parar contentor ${container.id}`,
                                  url: `containers/${container.id}`,
                                  successCallback: this.onDeleteSuccess,
                                  failureCallback: this.onDeleteFailure
                              }}
                              loading={loading}
                              topContextMenuItems={this.topContextMenu()}
                              bottomContextMenuItems={this.bottomContextMenu()}>
            {!!container.managerId &&
            <CardItem key={'managerId'}
                      label={'Manager'}
                      value={`${container.managerId}`}/>}
            <CardItem key={'type'}
                      label={'Type'}
                      value={`${container.type}`}/>
            <CardItem key={'name'}
                      label={'Name'}
                      value={container.name}/>
            <CardItem key={'image'}
                      label={'Image'}
                      value={container.image}/>
            <CardItem key={'hostname'}
                      label={'Hostname'}
                      value={container.publicIpAddress}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${container.ports.map(p => `${p.publicPort}:${p.privatePort}`).join('/')}`}/>
            <CardItem key={'region'}
                      label={'Region'}
                      value={`${container.region.region}`}/>
            <CardItem key={'coordinates'}
                      label={'Coordinates'}
                      value={`(${container.coordinates.latitude.toFixed(3)}, ${container.coordinates.longitude.toFixed(3)})`}/>
            <CardItem key={'serviceType'}
                      label={'Service type'}
                      value={`${container.labels['serviceType']}`}/>
        </CardContainer>
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteContainer,
    addContainers,
};

export default connect(null, mapDispatchToProps)(ContainerCard);
