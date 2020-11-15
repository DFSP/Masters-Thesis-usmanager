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

import IDatabaseData from "../../../../components/IDatabaseData";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../../components/BaseComponent";
import Form, {IFields, requiredAndTrimmed} from "../../../../components/form/Form";
import LoadingSpinner from "../../../../components/list/LoadingSpinner";
import {Error} from "../../../../components/errors/Error";
import Field from "../../../../components/form/Field";
import Tabs, {Tab} from "../../../../components/tabs/Tabs";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../../reducers";
import {
    addSimulatedHostMetric,
    addSimulatedHostMetricCloudHosts,
    addSimulatedHostMetricEdgeHosts,
    loadFields,
    loadSimulatedHostMetrics,
    updateSimulatedHostMetric
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedHostMetricCloudHostList from "./SimulatedHostMetricCloudHostList";
import SimulatedHostMetricEdgeHostList from "./SimulatedHostMetricEdgeHostList";

export interface ISimulatedHostMetric extends IDatabaseData {
    name: string;
    field: IField;
    minimumValue: number;
    maximumValue: number;
    override: boolean;
    generic: boolean;
    active: boolean;
    cloudHosts?: string[];
    edgeHosts?: string[];
}

const buildNewSimulatedHostMetric = (): Partial<ISimulatedHostMetric> => ({
    name: undefined,
    field: undefined,
    minimumValue: undefined,
    maximumValue: undefined,
    generic: undefined,
    override: true,
    active: true,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedHostMetric: Partial<ISimulatedHostMetric>;
    formSimulatedHostMetric?: Partial<ISimulatedHostMetric>;
    fields: { [key: string]: IField };
}

interface DispatchToProps {
    loadSimulatedHostMetrics: (name: string) => void;
    addSimulatedHostMetric: (simulatedHostMetric: ISimulatedHostMetric) => void;
    updateSimulatedHostMetric: (previousSimulatedHostMetric: ISimulatedHostMetric,
                                currentSimulatedHostMetric: ISimulatedHostMetric) => void;
    loadFields: () => void;
    addSimulatedHostMetricCloudHosts: (name: string, cloudHosts: string[]) => void;
    addSimulatedHostMetricEdgeHosts: (name: string, edgeHosts: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: ISimulatedHostMetric,
    selected: 'metric' | 'cloudHosts' | 'edgeHosts',
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    simulatedHostMetric?: ISimulatedHostMetric,
    formSimulatedHostMetric?: ISimulatedHostMetric,
    unsavedCloudHosts: string[],
    unsavedEdgeHosts: string[],
    isGeneric: boolean,
}

class SimulatedHostMetric extends BaseComponent<Props, State> {

    state: State = {
        unsavedEdgeHosts: [],
        unsavedCloudHosts: [],
        isGeneric: this.props.simulatedHostMetric?.generic || false,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadSimulatedHostMetric();
        this.props.loadFields();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
                <div className="container">
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadSimulatedHostMetric = () => {
        if (!this.isNew()) {
            const name = this.props.match.params.name;
            this.props.loadSimulatedHostMetrics(name);
        }
    };

    private getSimulatedHostMetric = () =>
        this.props.simulatedHostMetric || this.state.simulatedHostMetric;

    private getFormSimulatedHostMetric = () =>
        this.props.formSimulatedHostMetric || this.state.formSimulatedHostMetric;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ISimulatedHostMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">A métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addSimulatedHostMetric(simulatedMetric);
        this.saveEntities(simulatedMetric);
        if (this.mounted) {
            this.updateSimulatedHostMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPostFailure = (reason: string, simulatedHostMetric: ISimulatedHostMetric): void =>
        super.toast(`Não foi possível guardar a métrica simulada <b>${simulatedHostMetric.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<ISimulatedHostMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">As alterações à métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(simulatedMetric);
        const previousSimulatedHostMetric = this.getSimulatedHostMetric();
        if (previousSimulatedHostMetric.id) {
            this.props.updateSimulatedHostMetric(previousSimulatedHostMetric as ISimulatedHostMetric, simulatedMetric);
        }
        if (this.mounted) {
            this.updateSimulatedHostMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPutFailure = (reason: string, simulatedMetric: ISimulatedHostMetric): void =>
        super.toast(`Não foi possível guardar as alterações feitas à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b class="white-text">${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/métricas simuladas/hosts`);
        }
    };

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedHostMetric): void =>
        super.toast(`Não foi possível remover a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedCloudHosts.length
        || !!this.state.unsavedEdgeHosts.length;

    private saveEntities = (simulatedMetric: ISimulatedHostMetric) => {
        this.saveSimulatedHostMetricCloudHosts(simulatedMetric);
        this.saveSimulatedHostMetricEdgeHosts(simulatedMetric);
    };

    private addSimulatedHostMetricCloudHost = (cloudHost: string): void => {
        this.setState({
            unsavedCloudHosts: this.state.unsavedCloudHosts.concat(cloudHost)
        });
    };

    private removeSimulatedHostMetricCloudHosts = (cloudHosts: string[]): void => {
        this.setState({
            unsavedCloudHosts: this.state.unsavedCloudHosts.filter(cloudHost => !cloudHosts.includes(cloudHost))
        });
    };

    private saveSimulatedHostMetricCloudHosts = (simulatedMetric: ISimulatedHostMetric): void => {
        const {unsavedCloudHosts} = this.state;
        if (unsavedCloudHosts.length) {
            postData(`simulated-metrics/hosts/${simulatedMetric.name}/cloud-hosts`, unsavedCloudHosts,
                () => this.onSaveCloudHostsSuccess(simulatedMetric),
                (reason) => this.onSaveCloudHostsFailure(simulatedMetric, reason));
        }
    };

    private onSaveCloudHostsSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
        this.props.addSimulatedHostMetricCloudHosts(simulatedMetric.name, this.state.unsavedCloudHosts);
        if (this.mounted) {
            this.setState({unsavedCloudHosts: []});
        }
    };

    private onSaveCloudHostsFailure = (simulatedMetric: ISimulatedHostMetric, reason: string): void =>
        super.toast(`Não foi possível guardar as instâncias associadas à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private addSimulatedHostMetricEdgeHost = (edgeHost: string): void => {
        this.setState({
            unsavedEdgeHosts: this.state.unsavedEdgeHosts.concat(edgeHost)
        });
    };

    private removeSimulatedHostMetricEdgeHosts = (edgeHosts: string[]): void => {
        this.setState({
            unsavedEdgeHosts: this.state.unsavedEdgeHosts.filter(edgeHost => !edgeHosts.includes(edgeHost))
        });
    };

    private saveSimulatedHostMetricEdgeHosts = (simulatedMetric: ISimulatedHostMetric): void => {
        const {unsavedEdgeHosts} = this.state;
        if (unsavedEdgeHosts.length) {
            postData(`simulated-metrics/hosts/${simulatedMetric.name}/edge-hosts`, unsavedEdgeHosts,
                () => this.onSaveEdgeHostsSuccess(simulatedMetric),
                (reason) => this.onSaveEdgeHostsFailure(simulatedMetric, reason));
        }
    };

    private onSaveEdgeHostsSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
        this.props.addSimulatedHostMetricEdgeHosts(simulatedMetric.name, this.state.unsavedEdgeHosts);
        if (this.mounted) {
            this.setState({unsavedEdgeHosts: []});
        }
    };

    private onSaveEdgeHostsFailure = (simulatedMetric: ISimulatedHostMetric, reason: string): void =>
        super.toast(`Não foi possível guardar os hosts associados à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/hosts/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private updateSimulatedHostMetric = (simulatedHostMetric: ISimulatedHostMetric) => {
        simulatedHostMetric = Object.values(normalize(simulatedHostMetric, Schemas.SIMULATED_HOST_METRIC).entities.simulatedHostMetrics || {})[0];
        const formSimulatedHostMetric = {...simulatedHostMetric};
        removeFields(formSimulatedHostMetric);
        this.setState({
            simulatedHostMetric: {...this.getSimulatedHostMetric(), ...simulatedHostMetric},
            formSimulatedHostMetric: formSimulatedHostMetric
        });
    };

    private getFields = (simulatedHostMetric: Partial<ISimulatedHostMetric>): IFields =>
        Object.entries(simulatedHostMetric).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: key === 'generic' || key === 'active' || key === 'override' ? undefined : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private fieldOption = (field: IField): string =>
        field.name;

    private isGenericSelected = (generic: boolean) =>
        this.setState({isGeneric: generic});

    private simulatedHostMetric = () => {
        const {isLoading, error} = this.props;
        const simulatedHostMetric = this.getSimulatedHostMetric();
        const formSimulatedHostMetric = this.getFormSimulatedHostMetric();
        // @ts-ignore
        const simulatedHostMetricKey: (keyof ISimulatedHostMetric) = formSimulatedHostMetric && Object.keys(formSimulatedHostMetric)[0];
        const isNewSimulatedHostMetric = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formSimulatedHostMetric && (
                    /*@ts-ignore*/
                    <Form id={simulatedHostMetricKey}
                          fields={this.getFields(formSimulatedHostMetric)}
                          values={simulatedHostMetric}
                          isNew={isNewSimulatedHostMetric}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'simulated-metrics/hosts',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `simulated-metrics/hosts/${simulatedHostMetric.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `simulated-metrics/hosts/${simulatedHostMetric.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.keys(formSimulatedHostMetric).map((key, index) =>
                            key === 'field'
                                ? <Field<IField> key='fields'
                                                 id='field'
                                                 label='field'
                                                 type='dropdown'
                                                 dropdown={{
                                                     defaultValue: "Selecionar o campo",
                                                     values: Object.values(this.props.fields),
                                                     optionToString: this.fieldOption,
                                                     emptyMessage: 'Náo há campos disponíveis'
                                                 }}/>
                                : key === 'minimumValue' || key === 'maximumValue'
                                ? <Field key={index}
                                         id={key}
                                         label={key}
                                         type={'number'}/>
                                : key === 'override'
                                    ? <Field key={index}
                                             id={key}
                                             type='checkbox'
                                             checkbox={{label: 'Sobrepor às métricas obtidas'}}/>
                                    : key === 'generic'
                                        ? <Field key={index}
                                                 id={key}
                                                 type='checkbox'
                                                 checkbox={{label: 'Aplicar a todos os hosts', checkCallback: this.isGenericSelected}}/>
                                        : key === 'active'
                                            ? <Field key={index}
                                                     id={key}
                                                     type='checkbox'
                                                     checkbox={{label: 'ativo'}}/>
                                            : <Field key={index}
                                                     id={key}
                                                     label={key}/>
                        )}
                    </Form>
                )}
            </>
        )
    };

    private cloudHosts = (): JSX.Element =>
        <SimulatedHostMetricCloudHostList isLoadingSimulatedHostMetric={this.props.isLoading}
                                          loadSimulatedHostMetricError={this.props.error}
                                          simulatedHostMetric={this.getSimulatedHostMetric()}
                                          unsavedCloudHosts={this.state.unsavedCloudHosts}
                                          onAddCloudHost={this.addSimulatedHostMetricCloudHost}
                                          onRemoveCloudHosts={this.removeSimulatedHostMetricCloudHosts}/>;

    private edgeHosts = (): JSX.Element =>
        <SimulatedHostMetricEdgeHostList isLoadingSimulatedHostMetric={this.props.isLoading}
                                         loadSimulatedHostMetricError={this.props.error}
                                         simulatedHostMetric={this.getSimulatedHostMetric()}
                                         unsavedEdgeHosts={this.state.unsavedEdgeHosts}
                                         onAddEdgeHost={this.addSimulatedHostMetricEdgeHost}
                                         onRemoveEdgeHosts={this.removeSimulatedHostMetricEdgeHosts}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Métricas simuladas',
            id: 'simulatedHostMetric',
            content: () => this.simulatedHostMetric(),
            active: this.props.location.state?.selected === 'metric'
        },
        {
            title: 'Instâncias cloud',
            id: 'cloudHosts',
            content: () => this.cloudHosts(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'cloudHosts'
        },
        {
            title: 'Edge hosts',
            id: 'edgeHosts',
            content: () => this.edgeHosts(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'edgeHosts'
        },
    ];

}

function removeFields(simulatedHostMetric: Partial<ISimulatedHostMetric>) {
    delete simulatedHostMetric["id"];
    delete simulatedHostMetric["cloudHosts"];
    delete simulatedHostMetric["edgeHosts"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.simulatedMetrics.hosts.isLoadingSimulatedHostMetrics;
    const error = state.entities.simulatedMetrics.hosts.loadSimulatedHostMetricsError;
    const name = props.match.params.name;
    const simulatedHostMetric = isNew(props.location.search) ? buildNewSimulatedHostMetric() : state.entities.simulatedMetrics.hosts.data[name];
    let formSimulatedHostMetric;
    if (simulatedHostMetric) {
        formSimulatedHostMetric = {...simulatedHostMetric};
        removeFields(formSimulatedHostMetric);
    }
    const fields = state.entities.fields.data;
    return {
        isLoading,
        error,
        simulatedHostMetric,
        formSimulatedHostMetric,
        fields
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedHostMetrics,
    addSimulatedHostMetric,
    updateSimulatedHostMetric,
    loadFields,
    addSimulatedHostMetricCloudHosts,
    addSimulatedHostMetricEdgeHosts
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedHostMetric);