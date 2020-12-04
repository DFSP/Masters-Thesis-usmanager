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

import BaseComponent from "../../../components/BaseComponent";
import React from "react";
import {RouteComponentProps} from "react-router";
import Form, {IFields, requiredAndNumberAndMin, requiredAndTrimmed} from "../../../components/form/Form";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addKafkaBrokers, loadNodes, loadRegions, loadKafkaBrokers,} from "../../../actions";
import {connect} from "react-redux";
import {IRegion} from "../regions/Region";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {IContainer} from "../containers/Container";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IHostAddress} from "../hosts/Hosts";
import {INode} from "../nodes/Node";
import IDatabaseData from "../../../components/IDatabaseData";
import {IRuleCondition} from "../rules/conditions/RuleCondition";

export interface IKafkaBroker extends IDatabaseData {
    brokerId: number,
    container: IContainer,
    region: IRegion,
}

interface INewKafkaBrokerRegion {
    regions: string[] | undefined
}

interface INewKafkaBrokerHost {
    hostAddress: IHostAddress | undefined
}

const buildNewKafkaBrokerRegion = (): INewKafkaBrokerRegion => ({
    regions: undefined
});

const buildNewKafkaBrokerHost = (): INewKafkaBrokerHost => ({
    hostAddress: undefined
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    newKafkaBrokerHost?: INewKafkaBrokerHost;
    newKafkaBrokerRegion?: INewKafkaBrokerRegion;
    kafkaBroker?: IKafkaBroker;
    formKafkaBroker?: Partial<IKafkaBroker>;
    regions: { [key: string]: IRegion };
    nodes: { [key: string]: INode };
    kafkaBrokers: { [key: string]: IKafkaBroker };
}

interface DispatchToProps {
    loadKafkaBrokers: (id?: string) => void;
    addKafkaBrokers: (kafkaBrokers: IKafkaBroker[]) => void;
    loadRegions: () => void;
    loadNodes: () => void;
}

interface MatchParams {
    id: string;
}

interface LocationState {
    data: IKafkaBroker,
    selected: 'kafka-broker';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    kafkaBroker?: IKafkaBroker,
    formKafkaBroker?: IKafkaBroker,
    currentForm: 'Por regiões' | 'Num endereço',
}

class KafkaBroker extends BaseComponent<Props, State> {

    state: State = {
        currentForm: 'Por regiões'
    };
    private mounted = false;

    public componentDidMount(): void {
        if (isNew(this.props.location.search)) {
            this.props.loadKafkaBrokers();
        } else {
            const workerManagerId = this.props.match.params.id;
            this.props.loadKafkaBrokers(workerManagerId);
        }
        this.props.loadRegions();
        this.props.loadNodes();
        this.mounted = true;
    }

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private getKafkaBroker = () =>
        this.props.kafkaBroker || this.state.kafkaBroker;

    private getFormKafkaBroker = () =>
        this.props.formKafkaBroker || this.state.formKafkaBroker;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IKafkaBroker[]>): void => {
        let kafkaBrokers = reply.data;
        if (kafkaBrokers.length === 1) {
            const kafkaBroker = kafkaBrokers[0];
            super.toast(`<span class="green-text">O kafka broker foi iniciado no contentor ${this.mounted ? `<b class="white-text">${kafkaBroker.container.id}</b>` : `<a href='/kafka/${kafkaBroker.brokerId}'><b>${kafkaBroker.container.id}</b></a>`}</span>`);
            if (this.mounted) {
                this.updateKafkaBroker(kafkaBroker);
                this.props.history.replace(kafkaBroker.brokerId.toString())
            }
        } else {
            kafkaBrokers = kafkaBrokers.reverse();
            super.toast(`<span class="green-text">Foram iniciados ${kafkaBrokers.length} kafka brokers:<br/><b class="white-text">${kafkaBrokers.map(kafkaBroker => `Contentor ${kafkaBroker.container.id} => Host ${kafkaBroker.container.publicIpAddress}`).join('<br/>')}</b></span>`);
            if (this.mounted) {
                this.props.history.push('/kafka');
            }
        }
        this.props.addKafkaBrokers(kafkaBrokers);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Não foi possível lançar o kafka broker`, 10000, reason, true);

    private onDeleteSuccess = (kafkaBroker: IKafkaBroker): void => {
        super.toast(`<span class="green-text">O kafka broker <b class="white-text">${kafkaBroker.brokerId}</b> foi parado com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push('/kafka');
        }
    };

    private onDeleteFailure = (reason: string, kafkaBroker: IKafkaBroker): void =>
        super.toast(`Não foi possível parar o kafka broker ${this.mounted ? `<b>${kafkaBroker.brokerId}</b>` : `<a href='/kafka/${kafkaBroker.brokerId}'><b>${kafkaBroker.brokerId}</b></a>`}`, 10000, reason, true);

    private updateKafkaBroker = (kafkaBroker: IKafkaBroker) => {
        kafkaBroker = Object.values(normalize(kafkaBroker, Schemas.KAFKA_BROKER).entities.kafkaBrokers || {})[0];
        const formKafkaBroker = {...kafkaBroker};
        removeFields(formKafkaBroker);
        this.setState({kafkaBroker: kafkaBroker, formKafkaBroker: formKafkaBroker});
    };

    private getFields = (kafkaBroker: INewKafkaBrokerRegion | INewKafkaBrokerHost | IKafkaBroker): IFields =>
        Object.entries(kafkaBroker).map(([key, value]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: getTypeFromValue(value) === 'number'
                        ? {rule: requiredAndNumberAndMin, args: 0}
                        : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private getSelectableHosts = (): Partial<IHostAddress>[] =>
        Object.entries(this.props.nodes)
            .filter(([_, node]) => node.state === 'ready' && node.labels['place'] === 'CLOUD')
            .map(([_, node]) =>
                ({
                    username: node.labels['username'],
                    publicIpAddress: node.publicIpAddress,
                    privateIpAddress: node.labels['privateIpAddress'],
                    coordinates: node.labels['coordinates'] ? JSON.parse(node.labels['coordinates']) : undefined,
                }));

    private hostAddressesDropdown = (hostAddress: Partial<IHostAddress>): string =>
        hostAddress.publicIpAddress + (hostAddress.privateIpAddress ? ("/" + hostAddress.privateIpAddress) : '') + " - " + hostAddress.coordinates?.label;

    private regionOption = (region: IRegion) =>
        region.region;

    private getSelectableRegions = (): string[] => {
        const deployedRegions = Object.values(this.props.kafkaBrokers).map(kafkaBroker => kafkaBroker.region.region);
        return Object.keys(this.props.regions).filter(region => !deployedRegions.includes(region));
    }

    private containerIdField = (container: IContainer) =>
        container.id.toString();

    private containerPublicIpAddressField = (container: IContainer) =>
        container.publicIpAddress;

    private formFields = (isNew: boolean, formKafkaBroker?: Partial<IKafkaBroker>) => {
        const {currentForm} = this.state;
        return (
            isNew ?
                currentForm === 'Por regiões'
                    ?
                    <Field key={'regions'}
                           id={'regions'}
                           label={'regions'}
                           type={'list'}
                           value={this.getSelectableRegions()}/>
                    :
                    <>
                        <Field<Partial<IHostAddress>> key={'hostAddress'}
                                                      id={'hostAddress'}
                                                      label={'hostAddress'}
                                                      type="dropdown"
                                                      dropdown={{
                                                          defaultValue: "Selecionar o endereço",
                                                          values: this.getSelectableHosts(),
                                                          optionToString: this.hostAddressesDropdown,
                                                          emptyMessage: 'Náo há hosts disponíveis'
                                                      }}/>
                    </>
                : formKafkaBroker && Object.entries(formKafkaBroker).map((([key, value], index) =>
                key === 'container'
                    ? <>
                        <Field<IContainer> key={index}
                                           id={key}
                                           label={key + " id"}
                                           valueToString={this.containerIdField}
                                           icon={{linkedTo: '/contentores/' + (formKafkaBroker as Partial<IKafkaBroker>).container?.id}}/>
                        <Field<IContainer>
                            key={5000}
                            id={key}
                            label={"host"}
                            valueToString={this.containerPublicIpAddressField}/>
                    </>
                    : key === 'region'
                    ? <Field<IRegion> key={index}
                                      id={key}
                                      type="dropdown"
                                      label={key}
                                      valueToString={this.regionOption}
                                      dropdown={{
                                          defaultValue: "Selecionar a região",
                                          emptyMessage: "Não há regiões disponíveis",
                                          values: [(formKafkaBroker as IKafkaBroker).region],
                                          optionToString: this.regionOption
                                      }}/>
                    : <Field key={index}
                             id={key}
                             label={key}/>))
        );
    };

    private switchForm = (formId: 'Por regiões' | 'Num endereço') =>
        this.setState({currentForm: formId});

    private onPost = (kafkaBrokers: IKafkaBroker[]) => {
        if (kafkaBrokers.length == 1) {
            return kafkaBrokers[0];
        }
        return kafkaBrokers;
    }

    private kafkaBroker = () => {
        const {isLoading, error, newKafkaBrokerRegion, newKafkaBrokerHost} = this.props;
        const {currentForm} = this.state;
        const isNewKafkaBroker = this.isNew();
        const kafkaBroker = isNewKafkaBroker ? (currentForm === 'Por regiões' ? newKafkaBrokerRegion : newKafkaBrokerHost) : this.getKafkaBroker();
        const formKafkaBroker = this.getFormKafkaBroker();
        // @ts-ignore
        const kafkaBrokerKey: (keyof IKafkaBroker) = formKafkaBroker && Object.keys(formKafkaBroker)[0];
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && kafkaBroker && (
                    /*@ts-ignore*/
                    <Form id={kafkaBrokerKey}
                          fields={this.getFields(kafkaBroker)}
                          values={kafkaBroker}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={false}
                          post={{
                              textButton: 'Executar',
                              url: 'kafka',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure,
                              result: this.onPost,
                          }}
                          delete={{
                              textButton: 'Parar',
                              url: `kafka/${(kafkaBroker as IKafkaBroker).brokerId}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                        // kafka brokers must be launched on cloud hosts and on a specific aws region
                        // to be able to assign it an elastic ip
                        /*switchDropdown={isNewKafkaBroker ? {
                            options: currentForm === 'Por regiões' ? ['Num endereço'] : ['Por regiões'],
                            onSwitch: this.switchForm
                        } : undefined}*/>
                        {this.formFields(isNewKafkaBroker, formKafkaBroker)}
                    </Form>
                )}
            </>
        )
    };

    private tabs = (): Tab[] => [
        {
            title: 'Kafka broker',
            id: 'kafkaBroker',
            content: () => this.kafkaBroker(),
            active: this.props.location.state?.selected === 'kafka-broker'
        },
    ];

}

function removeFields(kafkaBroker: Partial<IKafkaBroker>) {
    delete kafkaBroker["id"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.kafkaBrokers.isLoadingKafkaBrokers;
    const error = state.entities.kafkaBrokers.loadKafkaBrokersError;
    const id = props.match.params.id.toString();
    const newKafkaBroker = isNew(props.location.search);
    const newKafkaBrokerRegion = newKafkaBroker ? buildNewKafkaBrokerRegion() : undefined;
    const newKafkaBrokerHost = newKafkaBroker ? buildNewKafkaBrokerHost() : undefined;
    const kafkaBroker = !newKafkaBroker ? state.entities.kafkaBrokers.data[id] : undefined;
    let formKafkaBroker;
    if (kafkaBroker) {
        formKafkaBroker = {...kafkaBroker};
        removeFields(formKafkaBroker);
    }
    const regions = state.entities.regions.data;
    const nodes = state.entities.nodes.data;
    const kafkaBrokers = state.entities.kafkaBrokers.data;
    return {
        isLoading,
        error,
        newKafkaBrokerRegion,
        newKafkaBrokerHost,
        kafkaBroker,
        formKafkaBroker,
        regions,
        nodes,
        kafkaBrokers
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadKafkaBrokers,
    addKafkaBrokers,
    loadRegions,
    loadNodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(KafkaBroker);