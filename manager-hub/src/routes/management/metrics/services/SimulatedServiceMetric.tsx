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
    addSimulatedServiceMetric,
    addSimulatedServiceMetricServices,
    loadFields,
    loadSimulatedServiceMetrics,
    updateSimulatedServiceMetric
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedServiceMetricServiceList from "./SimulatedServiceMetricServiceList";

export interface ISimulatedServiceMetric extends IDatabaseData {
    name: string;
    field: IField;
    minimumValue: number;
    maximumValue: number;
    generic: boolean;
    override: boolean;
    active: boolean;
    services?: string[];
}

const buildNewSimulatedServiceMetric = (): Partial<ISimulatedServiceMetric> => ({
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
    simulatedServiceMetric: Partial<ISimulatedServiceMetric>;
    formSimulatedServiceMetric?: Partial<ISimulatedServiceMetric>;
    fields: { [key: string]: IField };
}

interface DispatchToProps {
    loadSimulatedServiceMetrics: (name: string) => void;
    addSimulatedServiceMetric: (simulatedServiceMetric: ISimulatedServiceMetric) => void;
    updateSimulatedServiceMetric: (previousSimulatedServiceMetric: ISimulatedServiceMetric,
                                   currentSimulatedServiceMetric: ISimulatedServiceMetric) => void;
    loadFields: () => void;
    addSimulatedServiceMetricServices: (name: string, services: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: ISimulatedServiceMetric,
    selected: 'metric' | 'services',
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    simulatedServiceMetric?: ISimulatedServiceMetric,
    formSimulatedServiceMetric?: ISimulatedServiceMetric,
    unsavedServices: string[],
    isGeneric: boolean,
}

class SimulatedServiceMetric extends BaseComponent<Props, State> {

    state: State = {
        unsavedServices: [],
        isGeneric: this.props.simulatedServiceMetric?.generic || false,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadSimulatedServiceMetric();
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

    private loadSimulatedServiceMetric = () => {
        if (!isNew(this.props.location.search)) {
            const name = this.props.match.params.name;
            this.props.loadSimulatedServiceMetrics(name);
        }
    };

    private getSimulatedServiceMetric = () =>
        this.props.simulatedServiceMetric || this.state.simulatedServiceMetric;

    private getFormSimulatedServiceMetric = () =>
        this.props.formSimulatedServiceMetric || this.state.formSimulatedServiceMetric;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ISimulatedServiceMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">A métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/serviços/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addSimulatedServiceMetric(simulatedMetric);
        this.saveEntities(simulatedMetric);
        if (this.mounted) {
            this.updateSimulatedServiceMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPostFailure = (reason: string, simulatedServiceMetric: ISimulatedServiceMetric): void =>
        super.toast(`Não foi possível guardar a métrica simulada <b>${simulatedServiceMetric.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<ISimulatedServiceMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">As alterações à métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/serviços/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(simulatedMetric);
        const previousSimulatedServiceMetric = this.getSimulatedServiceMetric();
        if (previousSimulatedServiceMetric.id) {
            this.props.updateSimulatedServiceMetric(previousSimulatedServiceMetric as ISimulatedServiceMetric, simulatedMetric);
        }
        if (this.mounted) {
            this.updateSimulatedServiceMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPutFailure = (reason: string, simulatedMetric: ISimulatedServiceMetric): void =>
        super.toast(`Não foi possível atualizar a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/serviços/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (simulatedMetric: ISimulatedServiceMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b class="white-text">${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/métricas simuladas/serviços`);
        }
    };

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedServiceMetric): void =>
        super.toast(`Não foi possível remover a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/serviços/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedServices.length;

    private saveEntities = (simulatedMetric: ISimulatedServiceMetric) => {
        this.saveSimulatedServiceMetricServices(simulatedMetric);
    };

    private addSimulatedServiceMetricService = (service: string): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.concat(service)
        });
    };

    private removeSimulatedServiceMetricServices = (services: string[]): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.filter(service => !services.includes(service))
        });
    };

    private saveSimulatedServiceMetricServices = (simulatedMetric: ISimulatedServiceMetric): void => {
        const {unsavedServices} = this.state;
        if (unsavedServices.length) {
            postData(`simulated-metrics/services/${simulatedMetric.name}/services`, unsavedServices,
                () => this.onSaveServicesSuccess(simulatedMetric),
                (reason) => this.onSaveServicesFailure(simulatedMetric, reason));
        }
    };

    private onSaveServicesSuccess = (simulatedMetric: ISimulatedServiceMetric): void => {
        this.props.addSimulatedServiceMetricServices(simulatedMetric.name, this.state.unsavedServices);
        if (this.mounted) {
            this.setState({unsavedServices: []});
        }
    };

    private onSaveServicesFailure = (simulatedMetric: ISimulatedServiceMetric, reason: string): void =>
        super.toast(`Não foi possível guardar os serviços associados à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href='/métricas simuladas/serviços/${simulatedMetric.name}'><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private updateSimulatedServiceMetric = (simulatedServiceMetric: ISimulatedServiceMetric) => {
        simulatedServiceMetric = Object.values(normalize(simulatedServiceMetric, Schemas.SIMULATED_SERVICE_METRIC).entities.simulatedServiceMetrics || {})[0];
        const formSimulatedServiceMetric = {...simulatedServiceMetric};
        removeFields(formSimulatedServiceMetric);
        this.setState({
            simulatedServiceMetric: simulatedServiceMetric,
            formSimulatedServiceMetric: formSimulatedServiceMetric
        });
    };

    private getFields = (simulatedServiceMetric: Partial<ISimulatedServiceMetric>): IFields =>
        Object.entries(simulatedServiceMetric).map(([key, _]) => {
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

    private simulatedServiceMetric = () => {
        const {isLoading, error} = this.props;
        const simulatedServiceMetric = this.getSimulatedServiceMetric();
        const formSimulatedServiceMetric = this.getFormSimulatedServiceMetric();
        // @ts-ignore
        const simulatedServiceMetricKey: (keyof ISimulatedServiceMetric) = formSimulatedServiceMetric && Object.keys(formSimulatedServiceMetric)[0];
        const isNewSimulatedServiceMetric = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formSimulatedServiceMetric && (
                    /*@ts-ignore*/
                    <Form id={simulatedServiceMetricKey}
                          fields={this.getFields(formSimulatedServiceMetric)}
                          values={simulatedServiceMetric}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'simulated-metrics/services',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `simulated-metrics/services/${simulatedServiceMetric.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `simulated-metrics/services/${simulatedServiceMetric.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.keys(formSimulatedServiceMetric).map((key, index) =>
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
                                                 checkbox={{
                                                     label: 'Aplicar a todos os serviços',
                                                     checkCallback: this.isGenericSelected
                                                 }}/>
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

    private services = (): JSX.Element =>
        <SimulatedServiceMetricServiceList isLoadingSimulatedServiceMetric={this.props.isLoading}
                                           loadSimulatedServiceMetricError={this.props.error}
                                           simulatedServiceMetric={this.getSimulatedServiceMetric()}
                                           unsavedServices={this.state.unsavedServices}
                                           onAddService={this.addSimulatedServiceMetricService}
                                           onRemoveServices={this.removeSimulatedServiceMetricServices}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Métricas simuladas',
            id: 'simulatedServiceMetric',
            content: () => this.simulatedServiceMetric(),
            active: this.props.location.state?.selected === 'metric'
        },
        {
            title: 'Serviços',
            id: 'services',
            content: () => this.services(),
            disabled: this.state.isGeneric,
            active: this.props.location.state?.selected === 'services'
        },
    ];

}

function removeFields(simulatedServiceMetric: Partial<ISimulatedServiceMetric>) {
    delete simulatedServiceMetric["id"];
    delete simulatedServiceMetric["services"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.simulatedMetrics.services.isLoadingSimulatedServiceMetrics;
    const error = state.entities.simulatedMetrics.services.loadSimulatedServiceMetricsError;
    const name = props.match.params.name;
    const simulatedServiceMetric = isNew(props.location.search) ? buildNewSimulatedServiceMetric() : state.entities.simulatedMetrics.services.data[name];
    let formSimulatedServiceMetric;
    if (simulatedServiceMetric) {
        formSimulatedServiceMetric = {...simulatedServiceMetric};
        removeFields(formSimulatedServiceMetric);
    }
    const fields = state.entities.fields.data;
    return {
        isLoading,
        error,
        simulatedServiceMetric,
        formSimulatedServiceMetric,
        fields
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedServiceMetrics,
    addSimulatedServiceMetric,
    updateSimulatedServiceMetric,
    loadFields,
    addSimulatedServiceMetricServices,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedServiceMetric);