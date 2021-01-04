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

import {IDatabaseData} from "../../../../components/IData";
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
    addSimulatedContainerMetric,
    addSimulatedContainerMetricContainers,
    loadFields,
    loadSimulatedContainerMetrics,
    updateSimulatedContainerMetric
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedContainerMetricContainerList from "./SimulatedContainerMetricContainerList";

export interface ISimulatedContainerMetric extends IDatabaseData {
    name: string;
    field: IField;
    minimumValue: number;
    maximumValue: number;
    override: boolean;
    active: boolean;
    containers?: string[];
}

const buildNewSimulatedContainerMetric = (): Partial<ISimulatedContainerMetric> => ({
    name: undefined,
    field: undefined,
    minimumValue: undefined,
    maximumValue: undefined,
    override: true,
    active: true,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    simulatedContainerMetric: Partial<ISimulatedContainerMetric>;
    formSimulatedContainerMetric?: Partial<ISimulatedContainerMetric>;
    fields: { [key: string]: IField };
}

interface DispatchToProps {
    loadSimulatedContainerMetrics: (name: string) => void;
    addSimulatedContainerMetric: (simulatedContainerMetric: ISimulatedContainerMetric) => void;
    updateSimulatedContainerMetric: (previousSimulatedContainerMetric: ISimulatedContainerMetric,
                                     currentSimulatedContainerMetric: ISimulatedContainerMetric) => void;
    loadFields: () => void;
    addSimulatedContainerMetricContainers: (name: string, containers: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: ISimulatedContainerMetric,
    selected: 'metric' | 'containers',
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    simulatedContainerMetric?: ISimulatedContainerMetric,
    formSimulatedContainerMetric?: ISimulatedContainerMetric,
    unsavedContainersIds: string[],
    unsavedContainers: string[],
}

class SimulatedContainerMetric extends BaseComponent<Props, State> {

    state: State = {
        unsavedContainersIds: [],
        unsavedContainers: [],
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadSimulatedContainerMetric();
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

    private loadSimulatedContainerMetric = () => {
        if (!isNew(this.props.location.search)) {
            const name = this.props.match.params.name;
            this.props.loadSimulatedContainerMetrics(name);
        }
    };

    private getSimulatedContainerMetric = () =>
        this.props.simulatedContainerMetric || this.state.simulatedContainerMetric;

    private getFormSimulatedContainerMetric = () =>
        this.props.formSimulatedContainerMetric || this.state.formSimulatedContainerMetric;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ISimulatedContainerMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">A métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addSimulatedContainerMetric(simulatedMetric);
        this.saveEntities(simulatedMetric);
        if (this.mounted) {
            this.updateSimulatedContainerMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPostFailure = (reason: string, simulatedContainerMetric: ISimulatedContainerMetric): void =>
        super.toast(`Não foi possível guardar as métricas simuladas <b>${simulatedContainerMetric.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<ISimulatedContainerMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">As mudanças à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foram guardadas</span>`);
        this.saveEntities(simulatedMetric);
        const previousSimulatedContainerMetric = this.getSimulatedContainerMetric();
        if (previousSimulatedContainerMetric.id) {
            this.props.updateSimulatedContainerMetric(previousSimulatedContainerMetric as ISimulatedContainerMetric, simulatedMetric);
        }
        if (this.mounted) {
            this.updateSimulatedContainerMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPutFailure = (reason: string, simulatedMetric: ISimulatedContainerMetric): void =>
        super.toast(`Não foi possível atualizar a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (simulatedMetric: ISimulatedContainerMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b>${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/métricas simuladas/contentores`);
        }
    };

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedContainerMetric): void =>
        super.toast(`Não foi possível remover a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedContainersIds.length;

    private saveEntities = (simulatedMetric: ISimulatedContainerMetric) => {
        this.saveSimulatedContainerMetricContainers(simulatedMetric);
    };

    private addSimulatedContainerMetricContainer = (container: string): void => {
        const containerId = container.split(" - ")[1]
        this.setState({
            unsavedContainersIds: this.state.unsavedContainersIds.concat(containerId),
            unsavedContainers: this.state.unsavedContainers.concat(container)
        });
    };

    private removeSimulatedContainerMetricContainers = (containers: string[]): void => {
        const containersIds = containers.map(container => container.split(" - ")[1])
        this.setState({
            unsavedContainersIds: this.state.unsavedContainersIds.filter(container => !containersIds.includes(container)),
            unsavedContainers: this.state.unsavedContainers.filter(container => !containers.includes(container))
        });
    };

    private saveSimulatedContainerMetricContainers = (simulatedMetric: ISimulatedContainerMetric): void => {
        const {unsavedContainersIds} = this.state;
        if (unsavedContainersIds.length) {
            postData(`simulated-metrics/containers/${simulatedMetric.name}/containers`, unsavedContainersIds,
                () => this.onSaveContainersSuccess(simulatedMetric),
                (reason) => this.onSaveContainersFailure(simulatedMetric, reason));
        }
    };

    private onSaveContainersSuccess = (simulatedMetric: ISimulatedContainerMetric): void => {
        this.props.addSimulatedContainerMetricContainers(simulatedMetric.name, this.state.unsavedContainersIds);
        if (this.mounted) {
            this.setState({unsavedContainersIds: []});
        }
    };

    private onSaveContainersFailure = (simulatedMetric: ISimulatedContainerMetric, reason: string): void =>
        super.toast(`Não foi possível guardar os contentores associados à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/contentores/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private updateSimulatedContainerMetric = (simulatedContainerMetric: ISimulatedContainerMetric) => {
        simulatedContainerMetric = Object.values(normalize(simulatedContainerMetric, Schemas.SIMULATED_CONTAINER_METRIC).entities.simulatedContainerMetrics || {})[0];
        const formSimulatedContainerMetric = {...simulatedContainerMetric};
        removeFields(formSimulatedContainerMetric);
        this.setState({
            simulatedContainerMetric: simulatedContainerMetric,
            formSimulatedContainerMetric: formSimulatedContainerMetric
        });
    };

    private getFields = (simulatedContainerMetric: Partial<ISimulatedContainerMetric>): IFields =>
        Object.entries(simulatedContainerMetric).map(([key, _]) => {
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

    private simulatedContainerMetric = () => {
        const {isLoading, error} = this.props;
        const simulatedContainerMetric = this.getSimulatedContainerMetric();
        const formSimulatedContainerMetric = this.getFormSimulatedContainerMetric();
        // @ts-ignore
        const simulatedContainerMetricKey: (keyof ISimulatedContainerMetric) = formSimulatedContainerMetric && Object.keys(formSimulatedContainerMetric)[0];
        const isNewSimulatedContainerMetric = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formSimulatedContainerMetric && (
                    /*@ts-ignore*/
                    <Form id={simulatedContainerMetricKey}
                          fields={this.getFields(formSimulatedContainerMetric)}
                          values={simulatedContainerMetric}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'simulated-metrics/containers',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `simulated-metrics/containers/${simulatedContainerMetric.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `simulated-metrics/containers/${simulatedContainerMetric.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.keys(formSimulatedContainerMetric).map((key, index) =>
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
                                : key === 'override'
                                ? <Field key={index}
                                         id={key}
                                         label={key}
                                         type='checkbox'
                                         checkbox={{label: 'Sobrepor às métricas obtidas'}}/>
                                : key === 'active'
                                    ? <Field key={index}
                                             id={key}
                                             type='checkbox'
                                             checkbox={{label: 'ativo'}}/>
                                    : key === 'minimumValue' || key === 'maximumValue'
                                        ? <Field key={index}
                                                 id={key}
                                                 label={key}
                                                 type={'number'}/>
                                        : <Field key={index}
                                                 id={key}
                                                 label={key}/>
                        )}
                    </Form>
                )}
            </>
        )
    };

    private containers = (): JSX.Element =>
        <SimulatedContainerMetricContainerList isLoadingSimulatedContainerMetric={this.props.isLoading}
                                               loadSimulatedContainerMetricError={this.props.error}
                                               simulatedContainerMetric={this.getSimulatedContainerMetric()}
                                               unsavedContainersIds={this.state.unsavedContainersIds}
                                               unsavedContainers={this.state.unsavedContainers}
                                               onAddContainer={this.addSimulatedContainerMetricContainer}
                                               onRemoveContainers={this.removeSimulatedContainerMetricContainers}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Métricas simuladas',
            id: 'simulatedContainerMetric',
            content: () => this.simulatedContainerMetric(),
            active: this.props.location.state?.selected === 'metric'
        },
        {
            title: 'Contentores',
            id: 'containers',
            content: () => this.containers(),
            active: this.props.location.state?.selected === 'containers'
        },
    ];

}

function removeFields(simulatedContainerMetric: Partial<ISimulatedContainerMetric>) {
    delete simulatedContainerMetric["id"];
    delete simulatedContainerMetric["containers"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.simulatedMetrics.containers.isLoadingSimulatedContainerMetrics;
    const error = state.entities.simulatedMetrics.containers.loadSimulatedContainerMetricsError;
    const name = props.match.params.name;
    const simulatedContainerMetric = isNew(props.location.search) ? buildNewSimulatedContainerMetric() : state.entities.simulatedMetrics.containers.data[name];
    let formSimulatedContainerMetric;
    if (simulatedContainerMetric) {
        formSimulatedContainerMetric = {...simulatedContainerMetric};
        removeFields(formSimulatedContainerMetric);
    }
    const fields = state.entities.fields.data;
    return {
        isLoading,
        error,
        simulatedContainerMetric,
        formSimulatedContainerMetric,
        fields
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedContainerMetrics,
    addSimulatedContainerMetric,
    updateSimulatedContainerMetric,
    loadFields,
    addSimulatedContainerMetricContainers,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedContainerMetric);