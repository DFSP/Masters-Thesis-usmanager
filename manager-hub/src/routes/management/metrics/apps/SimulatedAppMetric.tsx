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
    addSimulatedAppMetric,
    addSimulatedAppMetricApps,
    loadFields,
    loadSimulatedAppMetrics,
    updateSimulatedAppMetric
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../../utils/api";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedAppMetricAppsList from "./SimulatedAppMetricAppsList";

export interface ISimulatedAppMetric extends IDatabaseData {
    name: string;
    field: IField;
    minimumValue: number;
    maximumValue: number;
    override: boolean;
    active: boolean;
    apps?: string[];
}

const buildNewSimulatedAppMetric = (): Partial<ISimulatedAppMetric> => ({
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
    simulatedAppMetric: Partial<ISimulatedAppMetric>;
    formSimulatedAppMetric?: Partial<ISimulatedAppMetric>;
    fields: { [key: string]: IField };
}

interface DispatchToProps {
    loadSimulatedAppMetrics: (name: string) => void;
    addSimulatedAppMetric: (simulatedAppMetric: ISimulatedAppMetric) => void;
    updateSimulatedAppMetric: (previousSimulatedAppMetric: ISimulatedAppMetric,
                               currentSimulatedAppMetric: ISimulatedAppMetric) => void;
    loadFields: () => void;
    addSimulatedAppMetricApps: (name: string, apps: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: ISimulatedAppMetric,
    selected: 'simulatedAppMetric' | 'apps',
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    simulatedAppMetric?: ISimulatedAppMetric,
    formSimulatedAppMetric?: ISimulatedAppMetric,
    unsavedApps: string[],
}

class SimulatedAppMetric extends BaseComponent<Props, State> {

    state: State = {
        unsavedApps: [],
    };
    private mounted = false;

    public componentDidMount(): void {
        this.loadSimulatedAppMetric();
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

    private loadSimulatedAppMetric = () => {
        if (!isNew(this.props.location.search)) {
            const name = this.props.match.params.name;
            this.props.loadSimulatedAppMetrics(name);
        }
    };

    private getSimulatedAppMetric = () =>
        this.props.simulatedAppMetric || this.state.simulatedAppMetric;

    private getFormSimulatedAppMetric = () =>
        this.props.formSimulatedAppMetric || this.state.formSimulatedAppMetric;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<ISimulatedAppMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">A métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/métricas simuladas/aplicações/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addSimulatedAppMetric(simulatedMetric);
        this.saveEntities(simulatedMetric);
        if (this.mounted) {
            this.updateSimulatedAppMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPostFailure = (reason: string, simulatedAppMetric: ISimulatedAppMetric): void =>
        super.toast(`Não foi possível guardar as métricas simuladas <b>${simulatedAppMetric.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<ISimulatedAppMetric>): void => {
        const simulatedMetric = reply.data;
        super.toast(`<span class="green-text">As alterações à métrica simulada ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/métricas simuladas/aplicações/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(simulatedMetric);
        const previousSimulatedAppMetric = this.getSimulatedAppMetric();
        if (previousSimulatedAppMetric.id) {
            this.props.updateSimulatedAppMetric(previousSimulatedAppMetric as ISimulatedAppMetric, simulatedMetric);
        }
        if (this.mounted) {
            this.updateSimulatedAppMetric(simulatedMetric);
            this.props.history.replace(simulatedMetric.name);
        }
    };

    private onPutFailure = (reason: string, simulatedMetric: ISimulatedAppMetric): void =>
        super.toast(`Náo foi possível atualizar a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/métricas simuladas/aplicações/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (simulatedMetric: ISimulatedAppMetric): void => {
        super.toast(`<span class="green-text">A métrica simulada <b class="white-text">${simulatedMetric.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/métricas simuladas/aplicações`);
        }
    };

    private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedAppMetric): void =>
        super.toast(`Não foi possível remover a métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/métricas simuladas/aplicações/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedApps.length;

    private saveEntities = (simulatedMetric: ISimulatedAppMetric) => {
        this.saveSimulatedAppMetricApps(simulatedMetric);
    };

    private addSimulatedAppMetricApp = (app: string): void => {
        this.setState({
            unsavedApps: this.state.unsavedApps.concat(app)
        });
    };

    private removeSimulatedAppMetricApps = (apps: string[]): void => {
        this.setState({
            unsavedApps: this.state.unsavedApps.filter(app => !apps.includes(app))
        });
    };

    private saveSimulatedAppMetricApps = (simulatedMetric: ISimulatedAppMetric): void => {
        const {unsavedApps} = this.state;
        if (unsavedApps.length) {
            postData(`simulated-metrics/apps/${simulatedMetric.name}/apps`, unsavedApps,
                () => this.onSaveAppsSuccess(simulatedMetric),
                (reason) => this.onSaveAppsFailure(simulatedMetric, reason));
        }
    };

    private onSaveAppsSuccess = (simulatedMetric: ISimulatedAppMetric): void => {
        this.props.addSimulatedAppMetricApps(simulatedMetric.name, this.state.unsavedApps);
        if (this.mounted) {
            this.setState({unsavedApps: []});
        }
    };

    private onSaveAppsFailure = (simulatedMetric: ISimulatedAppMetric, reason: string): void =>
        super.toast(`Não foi possível guardar as aplicações associadas à métrica simulada ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/métricas simuladas/aplicações/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`}`, 10000, reason, true);

    private updateSimulatedAppMetric = (simulatedAppMetric: ISimulatedAppMetric) => {
        simulatedAppMetric = Object.values(normalize(simulatedAppMetric, Schemas.SIMULATED_APP_METRIC).entities.simulatedAppMetrics || {})[0];
        const formSimulatedAppMetric = {...simulatedAppMetric};
        removeFields(formSimulatedAppMetric);
        this.setState({
            simulatedAppMetric: simulatedAppMetric,
            formSimulatedAppMetric: formSimulatedAppMetric
        });
    };

    private getFields = (simulatedAppMetric: Partial<ISimulatedAppMetric>): IFields =>
        Object.entries(simulatedAppMetric).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: {rule: requiredAndTrimmed}
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

    private simulatedAppMetric = () => {
        const {isLoading, error} = this.props;
        const simulatedAppMetric = this.getSimulatedAppMetric();
        const formSimulatedAppMetric = this.getFormSimulatedAppMetric();
        // @ts-ignore
        const simulatedAppMetricKey: (keyof ISimulatedAppMetric) = formSimulatedAppMetric && Object.keys(formSimulatedAppMetric)[0];
        const isNewSimulatedAppMetric = this.isNew();
        return (
            <>
                {!isNewSimulatedAppMetric && isLoading && <LoadingSpinner/>}
                {!isNewSimulatedAppMetric && !isLoading && error && <Error message={error}/>}
                {(isNewSimulatedAppMetric || !isLoading) && (isNewSimulatedAppMetric || !error) && formSimulatedAppMetric && (
                    /*@ts-ignore*/
                    <Form id={simulatedAppMetricKey}
                          fields={this.getFields(formSimulatedAppMetric)}
                          values={simulatedAppMetric}
                          isNew={isNew(this.props.location.search)}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'simulated-metrics/apps',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `simulated-metrics/apps/${simulatedAppMetric.name}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `simulated-metrics/apps/${simulatedAppMetric.name}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.keys(formSimulatedAppMetric).map((key, index) =>
                            key === 'field'
                                ? <Field<IField> key='fields'
                                                 id='field'
                                                 label='field'
                                                 type='dropdown'
                                                 dropdown={{
                                                     defaultValue: "Selecionar o campo",
                                                     values: Object.values(this.props.fields),
                                                     optionToString: this.fieldOption,
                                                     emptyMessage: 'Não há campos disponíveis'
                                                 }}/>
                                : key === 'override'
                                ? <Field key={index}
                                         id={key}
                                         type='checkbox'
                                         checkbox={{label: 'Sobrepor às métricas obtidas'}}/>
                                : key === 'active'
                                    ? <Field key={index}
                                             id={key}
                                             type='checkbox'
                                             checkbox={{label: 'Ativo'}}/>
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

    private apps = (): JSX.Element =>
        <SimulatedAppMetricAppsList isLoadingSimulatedAppMetric={this.props.isLoading}
                                    loadSimulatedAppMetricError={!this.isNew() ? this.props.error : undefined}
                                    simulatedAppMetric={this.getSimulatedAppMetric()}
                                    unsavedApps={this.state.unsavedApps}
                                    onAddApp={this.addSimulatedAppMetricApp}
                                    onRemoveApps={this.removeSimulatedAppMetricApps}/>;

    private tabs = (): Tab[] => [
        {
            title: 'Métricas simuladas',
            id: 'simulatedAppMetric',
            content: () => this.simulatedAppMetric(),
            active: this.props.location.state?.selected === 'simulatedAppMetric'
        },
        {
            title: 'Aplicações',
            id: 'apps',
            content: () => this.apps(),
            active: this.props.location.state?.selected === 'apps'
        },
    ];

}

function removeFields(simulatedAppMetric: Partial<ISimulatedAppMetric>) {
    delete simulatedAppMetric["id"];
    delete simulatedAppMetric["apps"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.simulatedMetrics.apps.isLoadingSimulatedAppMetrics;
    const error = state.entities.simulatedMetrics.apps.loadSimulatedAppMetricsError;
    const name = props.match.params.name;
    const simulatedAppMetric = isNew(props.location.search) ? buildNewSimulatedAppMetric() : state.entities.simulatedMetrics.apps.data[name];
    let formSimulatedAppMetric;
    if (simulatedAppMetric) {
        formSimulatedAppMetric = {...simulatedAppMetric};
        removeFields(formSimulatedAppMetric);
    }
    const fields = state.entities.fields.data;
    return {
        isLoading,
        error,
        simulatedAppMetric,
        formSimulatedAppMetric,
        fields
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadSimulatedAppMetrics,
    addSimulatedAppMetric,
    updateSimulatedAppMetric,
    loadFields,
    addSimulatedAppMetricApps,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedAppMetric);