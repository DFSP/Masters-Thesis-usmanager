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
import {IWorkerManager} from "./WorkerManager";
import BaseComponent from "../../../components/BaseComponent";
import LinkedContextMenuItem from "../../../components/contextmenu/LinkedContextMenuItem";
import {deleteWorkerManager} from "../../../actions";
import {connect} from "react-redux";

interface State {
    loading: boolean;
}

interface WorkerManagerCardProps {
    workerManager: IWorkerManager;
}

interface DispatchToProps {
    deleteWorkerManager: (workerManager: IWorkerManager) => void;
}

type Props = DispatchToProps & WorkerManagerCardProps;

class WorkerManagerCard extends BaseComponent<Props, State> {

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

    private onStopSuccess = (workerManager: IWorkerManager): void => {
        super.toast(`<span class="green-text">O Gestor local <b class="white-text">${workerManager.id}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteWorkerManager(workerManager)
    }

    private onStopFailure = (reason: string, workerManager: IWorkerManager): void => {
        super.toast(`Não foi possível para o gestor local <a href='/gestores locais/${workerManager.id}'><b>${workerManager.id}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }

    private contextMenu = (): JSX.Element[] => {
        const {workerManager} = this.props;
        return [
            <LinkedContextMenuItem
                option={'Ir para o contentor associado'}
                pathname={`/contentores/${workerManager.containerId}`}
                state={workerManager}/>,
            <LinkedContextMenuItem
                option={'Ver os hosts geridos'}
                pathname={`/gestores locais/${workerManager.id}`}
                selected={'managedHosts'}
                state={workerManager}/>,
            <LinkedContextMenuItem
                option={'Ver os contentores geridos'}
                pathname={`/gestores locais/${workerManager.id}`}
                selected={'managedContainers'}
                state={workerManager}/>,
        ];
    }

    public render() {
        const {workerManager} = this.props;
        const {loading} = this.state;
        const CardWorkerManager = Card<IWorkerManager>();
        return <CardWorkerManager id={`worker-manager-${workerManager.id}`}
                                  title={workerManager.id.toString()}
                                  link={{to: {pathname: `/gestores locais/${workerManager.id}`, state: workerManager}}}
                                  height={'175px'}
                                  margin={'10px 0'}
                                  hoverable
                                  delete={{
                                      textButton: workerManager.state === 'ready' ? 'Parar' : 'Remover',
                                      url: `worker-managers/${workerManager?.id}`,
                                      successCallback: this.onStopSuccess,
                                      failureCallback: this.onStopFailure,
                                  }}
                                  loading={loading}
                                  bottomContextMenuItems={this.contextMenu()}>
            <CardItem key={'state'}
                      label={'State'}
                      value={workerManager.state}/>
            <CardItem key={'region'}
                      label={'Region'}
                      value={workerManager.region.region}/>
            <CardItem key={'container'}
                      label={'Container'}
                      value={workerManager.containerId}/>
            <CardItem key={'host'}
                      label={'Host'}
                      value={workerManager.publicIpAddress}/>
        </CardWorkerManager>
    }

}

const mapDispatchToProps: DispatchToProps = {
    deleteWorkerManager
};

export default connect(null, mapDispatchToProps)(WorkerManagerCard);
