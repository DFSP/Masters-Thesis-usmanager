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

import React from 'react';
import {RouteComponentProps} from 'react-router';
import Form, {IFields, requiredAndNumberAndMinAndMax, requiredAndTrimmed} from "../../../components/form/Form"
import IDatabaseData from "../../../components/IDatabaseData";
import {
    addService,
    addServiceApps,
    addServiceDependencies,
    addServicePredictions,
    addServiceRules,
    addServiceSimulatedMetrics,
    loadServices,
    updateService
} from "../../../actions";
import {connect} from "react-redux";
import MainLayout from "../../../views/mainLayout/MainLayout";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {ReduxState} from "../../../reducers";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import BaseComponent from "../../../components/BaseComponent";
import {Error} from "../../../components/errors/Error";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import {IReply, postData} from "../../../utils/api";
import ServiceAppList, {IAddServiceApp} from "./ServiceAppList";
import ServiceDependencyList from "./ServiceDependencyList";
import ServiceDependentList from "./ServiceDependentList";
import ServicePredictionList, {IPrediction} from "./ServicePredictionList";
import ServiceRuleList from "./ServiceRuleList";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import GenericServiceRuleList from "./GenericServiceRuleList";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import ServiceSimulatedMetricList from "./ServiceSimulatedMetricList";
import GenericServiceSimulatedMetricList from "./GenericSimulatedServiceMetricList";

export interface IService extends IDatabaseData {
    serviceName: string;
    dockerRepository: string;
    defaultExternalPort: number;
    defaultInternalPort: number;
    defaultDb?: string;
    launchCommand?: string;
    minimumReplicas: number;
    maximumReplicas: number;
    outputLabel?: string;
    serviceType: ServiceType;
    expectedMemoryConsumption: number;
    apps?: string[];
    dependencies?: string[];
    dependents?: string[];
    predictions?: { [key: string]: IPrediction };
    serviceRules?: string[];
    serviceSimulatedMetrics?: string[];
}

export type ServiceType = 'FRONTEND' | 'BACKEND' | 'DATABASE' | 'SYSTEM';

const buildNewService = (): Partial<IService> => ({
    serviceName: undefined,
    dockerRepository: undefined,
    defaultExternalPort: undefined,
    defaultInternalPort: undefined,
    defaultDb: undefined,
    launchCommand: undefined,
    minimumReplicas: undefined,
    maximumReplicas: undefined,
    outputLabel: undefined,
    serviceType: undefined,
    expectedMemoryConsumption: undefined,
});

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    service: Partial<IService>;
    formService?: Partial<IService>,
}

interface DispatchToProps {
    loadServices: (name: string) => void;
    addService: (service: IService) => void;
    updateService: (previousService: IService, currentService: IService) => void;
    addServiceApps: (serviceName: string, apps: string[]) => void;
    addServiceDependencies: (serviceName: string, dependencies: string[]) => void;
    addServicePredictions: (serviceName: string, predictions: IPrediction[]) => void;
    addServiceRules: (serviceName: string, rules: string[]) => void;
    addServiceSimulatedMetrics: (serviceName: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IService,
    selected: 'service' | 'apps' | 'dependencies' | 'dependents' | 'predictions' | 'serviceRules' | 'genericRules'
        | 'simulatedMetrics' | 'genericSimulatedMetrics'
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    service?: IService,
    formService?: IService,
    unsavedApps: IAddServiceApp[],
    unsavedDependencies: string[],
    unsavedPredictions: IPrediction[],
    unsavedRules: string[],
    unsavedSimulatedMetrics: string[],
}

class Service extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            unsavedApps: [],
            unsavedDependencies: [],
            unsavedPredictions: [],
            unsavedRules: [],
            unsavedSimulatedMetrics: [],
        };
    }

    public componentDidMount(): void {
        this.loadService();
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    public render() {
        return (
            <MainLayout>
                {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
                <div className='container'>
                    <Tabs {...this.props} tabs={this.tabs()}/>
                </div>
            </MainLayout>
        );
    }

    private loadService = () => {
        if (!this.isNew()) {
            const serviceName = this.props.match.params.name;
            this.props.loadServices(serviceName);
        }
    };

    private getService = () =>
        this.props.service || this.state.service;

    private getFormService = () =>
        this.props.formService || this.state.formService;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IService>): void => {
        const service = reply.data;
        super.toast(`<span class='green-text'>O serviço ${this.mounted ? `<b class='white-text'>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addService(service);
        this.saveEntities(service);
        if (this.mounted) {
            this.updateService(service);
            this.props.history.replace(service.serviceName);
        }
    };

    private onPostFailure = (reason: string, service: IService): void =>
        super.toast(`Não foi possível guardar o serviço <b>${service.serviceName}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IService>): void => {
        const service = reply.data;
        super.toast(`<span class='green-text'>As alterações ao serviço ${this.mounted ? `<b class='white-text'>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(service);
        const previousService = this.getService();
        if (previousService.id) {
            this.props.updateService(previousService as IService, service);
        }
        if (this.mounted) {
            this.updateService(service);
            this.props.history.replace(service.serviceName);
        }
    };

    private onPutFailure = (reason: string, service: IService): void =>
        super.toast(`Não foi possível guardar as alterações feitas ao serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (service: IService): void => {
        super.toast(`<span class='green-text'>O serviço <b class='white-text'>${service.serviceName}</b> foi removido com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/serviços`);
        }
    };

    private onDeleteFailure = (reason: string, service: IService): void =>
        super.toast(`Não foi possível apagar o serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedApps.length
        || !!this.state.unsavedDependencies.length
        || !!this.state.unsavedPredictions.length
        || !!this.state.unsavedRules.length
        || !!this.state.unsavedSimulatedMetrics.length;

    private saveEntities = (service: IService) => {
        this.saveServiceApps(service);
        this.saveServiceDependencies(service);
        this.saveServicePredictions(service);
        this.saveServiceRules(service);
        this.saveServiceSimulatedMetrics(service);
    };

    private addServiceApp = (app: IAddServiceApp): void => {
        this.setState({
            unsavedApps: this.state.unsavedApps.concat(app)
        });
    };

    private removeServiceApps = (apps: string[]): void => {
        this.setState({
            unsavedApps: this.state.unsavedApps.filter(app => !apps.includes(app.name))
        });
    };

    private saveServiceApps = (service: IService): void => {
        const {unsavedApps} = this.state;
        if (unsavedApps.length) {
            postData(`services/${service.serviceName}/apps`, unsavedApps,
                () => this.onSaveAppsSuccess(service),
                (reason) => this.onSaveAppsFailure(service, reason));
        }
    };

    private onSaveAppsSuccess = (service: IService): void => {
        this.props.addServiceApps(service.serviceName, this.state.unsavedApps.map(app => app.name));
        if (this.mounted) {
            this.setState({unsavedApps: []});
        }
    };

    private onSaveAppsFailure = (service: IService, reason: string): void =>
        super.toast(`Não foi possível guardar as aplicações associadas ao serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private addServiceDependency = (dependency: string): void => {
        this.setState({
            unsavedDependencies: this.state.unsavedDependencies.concat(dependency)
        });
    };

    private removeServiceDependencies = (dependencies: string[]): void => {
        this.setState({
            unsavedDependencies: this.state.unsavedDependencies.filter(dependency => !dependencies.includes(dependency))
        });
    };

    private saveServiceDependencies = (service: IService): void => {
        const {unsavedDependencies} = this.state;
        if (unsavedDependencies.length) {
            postData(`services/${service.serviceName}/dependencies`, unsavedDependencies,
                () => this.onSaveDependenciesSuccess(service),
                (reason) => this.onSaveDependenciesFailure(service, reason));
        }
    };

    private onSaveDependenciesSuccess = (service: IService): void => {
        this.props.addServiceDependencies(service.serviceName, this.state.unsavedDependencies);
        if (this.mounted) {
            this.setState({unsavedDependencies: []});
        }
    };

    private onSaveDependenciesFailure = (service: IService, reason: string): void =>
        super.toast(`Não foi possível guardar as dependências do serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private addServicePrediction = (prediction: IPrediction): void => {
        this.setState({
            unsavedPredictions: this.state.unsavedPredictions.concat(prediction)
        });
    };

    private removeServicePredictions = (predictions: string[]): void => {
        this.setState({
            unsavedPredictions: this.state.unsavedPredictions.filter(prediction => !predictions.includes(prediction.name))
        });
    };

    private saveServicePredictions = (service: IService): void => {
        const {unsavedPredictions} = this.state;
        if (unsavedPredictions.length) {
            postData(`services/${service.serviceName}/predictions`, unsavedPredictions,
                () => this.onSavePredictionsSuccess(service),
                (reason) => this.onSavePredictionsFailure(service, reason));
        }
    };

    private onSavePredictionsSuccess = (service: IService): void => {
        this.props.addServicePredictions(service.serviceName, this.state.unsavedPredictions);
        if (this.mounted) {
            this.setState({unsavedPredictions: []});
        }
    };

    private onSavePredictionsFailure = (service: IService, reason: string): void =>
        super.toast(`Não foi possível guardar as previsões atribuídas ao serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private addServiceRule = (rule: string): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.concat(rule)
        });
    };

    private removeServiceRules = (rules: string[]): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
        });
    };

    private saveServiceRules = (service: IService): void => {
        const {unsavedRules} = this.state;
        if (unsavedRules.length) {
            postData(`services/${service.serviceName}/rules`, unsavedRules,
                () => this.onSaveRulesSuccess(service),
                (reason) => this.onSaveRulesFailure(service, reason));
        }
    };

    private onSaveRulesSuccess = (service: IService): void => {
        this.props.addServiceRules(service.serviceName, this.state.unsavedRules);
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (service: IService, reason: string): void =>
        super.toast(`Não foi possível guardar as regras associadas ao serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private addServiceSimulatedMetric = (simulatedMetric: string): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
        });
    };

    private removeServiceSimulatedMetrics = (simulatedMetrics: string[]): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
        });
    };

    private saveServiceSimulatedMetrics = (service: IService): void => {
        const {unsavedSimulatedMetrics} = this.state;
        if (unsavedSimulatedMetrics.length) {
            postData(`services/${service.serviceName}/simulated-metrics`, unsavedSimulatedMetrics,
                () => this.onSaveSimulatedMetricsSuccess(service),
                (reason) => this.onSaveSimulatedMetricsFailure(service, reason));
        }
    };

    private onSaveSimulatedMetricsSuccess = (service: IService): void => {
        this.props.addServiceSimulatedMetrics(service.serviceName, this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (service: IService, reason: string): void =>
        super.toast(`Não foi possível guardar as métricas simuladas do serviço ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href='/serviços/${service.serviceName}'><b>${service.serviceName}</b></a>`}`, 10000, reason, true);

    private updateService = (service: IService) => {
        service = Object.values(normalize(service, Schemas.SERVICE).entities.services || {})[0];
        const formService = {...service};
        removeFields(formService);
        this.setState({service: service, formService: formService});
    };

    private getFields = (service: Partial<IService>): IFields =>
        Object.entries(service).map(([key, value]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation:
                        getTypeFromValue(value) === 'number'
                            ? {rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647]}
                            : !['defaultDb', 'launchCommand', 'minimumReplicas', 'maximumReplicas', 'expectedMemoryConsumption'].includes(key)
                            ? {rule: requiredAndTrimmed}
                            : undefined
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private service = () => {
        const {isLoading, error} = this.props;
        const service = this.getService();
        const formService = this.getFormService();
        // @ts-ignore
        const serviceKey: (keyof IService) = formService && Object.keys(formService)[0];
        const isNewService = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formService && (
                    /*@ts-ignore*/
                    <Form id={serviceKey}
                          fields={this.getFields(formService)}
                          values={service}
                          isNew={isNewService}
                          showSaveButton={this.shouldShowSaveButton()}
                          post={{
                              url: 'services',
                              successCallback: this.onPostSuccess,
                              failureCallback: this.onPostFailure
                          }}
                          put={{
                              url: `services/${service.serviceName}`,
                              successCallback: this.onPutSuccess,
                              failureCallback: this.onPutFailure
                          }}
                          delete={{
                              url: `services/${service.serviceName}`,
                              successCallback: this.onDeleteSuccess,
                              failureCallback: this.onDeleteFailure
                          }}
                          saveEntities={this.saveEntities}>
                        {Object.entries(formService).map(([key, value], index) =>
                            key === 'serviceType'
                                ? <Field key={index}
                                         id={key}
                                         type='dropdown'
                                         label={key}
                                         dropdown={{
                                             defaultValue: "Selecionar o tipo de serviço",
                                             values: ["FRONTEND", "BACKEND", "DATABASE", "SYSTEM"]
                                         }}/>
                                : <Field key={index}
                                         id={key}
                                         label={key}
                                         type={['defaultExternalPort', 'defaultInternalPort', 'minimumReplicas', 'maximumReplicas',
                                             'expectedMemoryConsumption'].includes(key) ? 'number' : 'text'}/>
                        )}
                    </Form>
                )}
            </>
        )
    };

    private apps = (): JSX.Element =>
        <ServiceAppList isLoadingService={this.props.isLoading}
                        loadServiceError={this.props.error}
                        service={this.getService()}
                        unsavedApps={this.state.unsavedApps}
                        onAddServiceApp={this.addServiceApp}
                        onRemoveServiceApps={this.removeServiceApps}/>;

    private dependencies = (): JSX.Element =>
        <ServiceDependencyList isLoadingService={this.props.isLoading}
                               loadServiceError={this.props.error}
                               service={this.getService()}
                               unsavedDependencies={this.state.unsavedDependencies}
                               onAddServiceDependency={this.addServiceDependency}
                               onRemoveServiceDependencies={this.removeServiceDependencies}/>;

    private dependents = (): JSX.Element =>
        <ServiceDependentList isLoadingService={this.props.isLoading}
                              loadServiceError={this.props.error}
                              service={this.getService()}/>;

    private predictions = (): JSX.Element =>
        <ServicePredictionList isLoadingService={this.props.isLoading}
                               loadServiceError={this.props.error}
                               service={this.getService()}
                               unsavedPredictions={this.state.unsavedPredictions}
                               onAddServicePrediction={this.addServicePrediction}
                               onRemoveServicePredictions={this.removeServicePredictions}/>;

    private rules = (): JSX.Element =>
        <ServiceRuleList isLoadingService={this.props.isLoading}
                         loadServiceError={this.props.error}
                         service={this.getService()}
                         unsavedRules={this.state.unsavedRules}
                         onAddServiceRule={this.addServiceRule}
                         onRemoveServiceRules={this.removeServiceRules}/>;

    private genericRules = (): JSX.Element =>
        <GenericServiceRuleList/>;

    private simulatedMetrics = (): JSX.Element =>
        <ServiceSimulatedMetricList isLoadingService={this.props.isLoading}
                                    loadServiceError={this.props.error}
                                    service={this.getService()}
                                    unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                    onAddSimulatedServiceMetric={this.addServiceSimulatedMetric}
                                    onRemoveSimulatedServiceMetrics={this.removeServiceSimulatedMetrics}/>;

    private genericSimulatedMetrics = (): JSX.Element =>
        <GenericServiceSimulatedMetricList/>;

    private tabs = (): Tab[] => ([
        {
            title: 'Serviços',
            id: 'service',
            content: () => this.service(),
            active: this.props.location.state?.selected === 'service'
        },
        {
            title: 'Aplicações',
            id: 'apps',
            content: () => this.apps(),
            active: this.props.location.state?.selected === 'apps'
        },
        {
            title: 'Dependências',
            id: 'dependencies',
            content: () => this.dependencies(),
            active: this.props.location.state?.selected === 'dependencies'
        },
        {
            title: 'Dependentes',
            id: 'dependents',
            content: () => this.dependents(),
            hidden: this.isNew(),
            active: this.props.location.state?.selected === 'dependents'
        },
        {
            title: 'Previsões',
            id: 'predictions',
            content: () => this.predictions(),
            active: this.props.location.state?.selected === 'predictions'
        },
        {
            title: 'Regras',
            id: 'serviceRules',
            content: () => this.rules(),
            active: this.props.location.state?.selected === 'serviceRules'
        },
        {
            title: 'Regras genéricas',
            id: 'genericRules',
            content: () => this.genericRules(),
            active: this.props.location.state?.selected === 'genericRules'
        },
        {
            title: 'Métricas simuladas',
            id: 'simulatedMetrics',
            content: () => this.simulatedMetrics(),
            active: this.props.location.state?.selected === 'simulatedMetrics'
        },
        {
            title: 'Métricas simuladas genéricas',
            id: 'genericSimulatedMetrics',
            content: () => this.genericSimulatedMetrics(),
            active: this.props.location.state?.selected === 'genericSimulatedMetrics'
        }
    ]);

}

function removeFields(service: Partial<IService>) {
    delete service["id"];
    delete service["apps"];
    delete service["dependencies"];
    delete service["dependents"];
    delete service["predictions"];
    delete service["serviceRules"];
    delete service["serviceSimulatedMetrics"];
    if (service.serviceType === 'SYSTEM') {
        delete service["minimumReplicas"];
        delete service["maximumReplicas"];
    }
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.services.isLoadingServices;
    const error = state.entities.services.loadServicesError;
    const name = props.match.params.name;
    const service = isNew(props.location.search) ? buildNewService() : state.entities.services.data[name];
    let formService;
    if (service) {
        formService = {...service};
        removeFields(formService);
    }
    return {
        isLoading,
        error,
        service,
        formService,
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadServices,
    updateService,
    addService,
    addServiceApps,
    addServiceDependencies,
    addServicePredictions,
    addServiceRules,
    addServiceSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(Service);
