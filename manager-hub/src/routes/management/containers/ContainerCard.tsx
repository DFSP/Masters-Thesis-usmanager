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
import {deleteContainer} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface ContainerCardProps {
    container: IContainer;
}

interface DispatchToProps {
    deleteContainer: (container: IContainer) => void;
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

    private onDeleteSuccess = (container: IContainer): void => {
        super.toast(`<span class="green-text">Container <b class="white-text">${container.containerId}</b> successfully stopped</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteContainer(container);
    }

    private onDeleteFailure = (reason: string, container: IContainer): void => {
        super.toast(`Unable to stop <a href=/containers/${container.containerId}><b>${container.containerId}</b></a> container`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {container} = this.props;
        return [
            <LinkedContextMenuItem
                option={'View ports'}
                pathname={`/containers/${container.containerId}`}
                selected={'ports'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'View labels'}
                pathname={`/containers/${container.containerId}`}
                selected={'containerLabels'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Check logs'}
                pathname={`/containers/${container.containerId}`}
                selected={'logs'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Modify rules'}
                pathname={`/containers/${container.containerId}`}
                selected={'rules'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'View generic rules'}
                pathname={`/containers/${container.containerId}`}
                selected={'genericContainerRules'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'Modify simulated metrics'}
                pathname={`/containers/${container.containerId}`}
                selected={'simulatedMetrics'}
                state={container}/>,
            <LinkedContextMenuItem
                option={'View generic simulated metrics'}
                pathname={`/containers/${container.containerId}`}
                selected={'genericSimulatedMetrics'}
                state={container}/>
        ];
    }

    public render() {
        const {container} = this.props;
        const {loading} = this.state;
        const CardContainer = Card<IContainer>();
        return <CardContainer id={`container-${container.containerId}`}
                              title={container.containerId.toString()}
                              link={{to: {pathname: `/containers/${container.containerId}`, state: container}}}
                              height={'215px'}
                              margin={'10px 0'}
                              hoverable
                              delete={{
                                  textButton: 'Stop',
                                  confirmMessage: `to stop container ${container.containerId}`,
                                  url: `containers/${container.containerId}`,
                                  successCallback: this.onDeleteSuccess,
                                  failureCallback: this.onDeleteFailure
                              }}
                              loading={loading}
                              bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'names'}
                      label={'Names'}
                      value={container.names.join(', ')}/>
            <CardItem key={'image'}
                      label={'Image'}
                      value={container.image}/>
            <CardItem key={'hostname'}
                      label={'Hostname'}
                      value={container.publicIpAddress}/>
            <CardItem key={'ports'}
                      label={'Ports'}
                      value={`${container.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
            <CardItem key={'coordinates'}
                      label={'Coordinates'}
                      value={`(${container.coordinates.latitude.toFixed(3)}, ${container.coordinates.longitude.toFixed(3)})`}/>
            <CardItem key={'type'}
                      label={'Type'}
                      value={`${container.labels['serviceType']}`}/>
        </CardContainer>
    }
}

const mapDispatchToProps: DispatchToProps = {
    deleteContainer
};

export default connect(null, mapDispatchToProps)(ContainerCard);
