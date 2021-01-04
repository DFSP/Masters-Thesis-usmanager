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

import {IDatabaseData} from "../../../components/IData";
import BaseComponent from "../../../components/BaseComponent";
import {RouteComponentProps} from "react-router";
import Form, {
    ICustomButton,
    IFields,
    IFormLoading,
    maxSizeAndTrimmed,
    requiredAndTrimmed,
} from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import LoadingSpinner from "../../../components/list/LoadingSpinner";
import {Error} from "../../../components/errors/Error";
import React from "react";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
    addApp,
    addAppRules,
    addAppServices,
    addAppSimulatedMetrics,
    loadApps, loadContainers,
    loadRegions,
    updateApp
} from "../../../actions";
import {connect} from "react-redux";
import AppServicesList, {IAddAppService, IAppService} from "./AppServicesList";
import {IReply, postData} from "../../../utils/api";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {isNew} from "../../../utils/router";
import {IRegion} from "../regions/Region";
import formStyles from "../../../components/form/Form.module.css";
import {IContainer} from "../containers/Container";
import LaunchAppDialog from "./LaunchAppDialog";
import {IMarker} from "../../../components/map/Marker";
import GenericServiceRuleList from "../services/GenericServiceRuleList";
import GenericSimulatedServiceMetricList from "../services/GenericSimulatedServiceMetricList";
import AppRuleList from "./AppRuleList";
import AppSimulatedMetricList from "./AppSimulatedMetricList";

export interface IApp extends IDatabaseData {
    name: string;
    description?: string;
    services?: { [key: string]: IAppService }
    appRules?: string[];
    appSimulatedMetrics?: string[];
}

const buildNewApp = (): Partial<IApp> => ({
    name: undefined,
    description: undefined,
});

interface ILaunchApp {
    [key: string]: IContainer[]
}

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    app: Partial<IApp>;
    formApp?: Partial<IApp>;
    regions: { [key: string]: IRegion };
    containers: { [key: string]: IContainer };
}

interface DispatchToProps {
    loadApps: (name: string) => void;
    loadRegions: () => void;
    loadContainers: () => void;
    addApp: (app: IApp) => void;
    updateApp: (previousApp: IApp, currentApp: IApp) => void;
    addAppServices: (appName: string, appServices: IAddAppService[]) => void;
    addAppRules: (name: string, rules: string[]) => void;
    addAppSimulatedMetrics: (name: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IApp,
    selected: 'app' | 'services' | 'rules' | 'genericRules' | 'simulatedMetrics' | 'genericSimulatedMetrics';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    app?: IApp,
    formApp?: IApp,
    loading: IFormLoading,
    services: IAddAppService[],
    unsavedServices: IAddAppService[],
    unsavedRules: string[],
    unsavedSimulatedMetrics: string[],
}

class App extends BaseComponent<Props, State> {

    state: State = {
        loading: undefined,
        services: [],
        unsavedServices: [],
        unsavedRules: [],
        unsavedSimulatedMetrics: [],
    };
    private mounted = false;

    public componentDidMount(): void {
        this.props.loadRegions();
        this.loadApp();
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

    private loadApp = () => {
        if (!isNew(this.props.location.search)) {
            const appName = this.props.match.params.name;
            this.props.loadApps(appName);
        }
    };

    private getApp = () =>
        this.props.app || this.state.app;

    private getFormApp = () =>
        this.props.formApp || this.state.formApp;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IApp>): void => {
        const app = reply.data;
        super.toast(`<span class='green-text'>A aplicação ${this.mounted ? `<b class='white-text'>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`} foi guardada com sucesso</span>`);
        this.props.addApp(app);
        this.saveEntities(app);
        if (this.mounted) {
            this.updateApp(app);
            this.props.history.replace(app.name);
        }
    };

    private onPostFailure = (reason: string, app: IApp): void =>
        super.toast(`Não foi possível guardar a aplicação <b>${app.name}</b>`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IApp>): void => {
        const app = reply.data;
        super.toast(`<span class='green-text'>As mudanças à aplicação ${this.mounted ? `<b class='white-text'>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`} foram guardadas com sucesso</span>`);
        this.saveEntities(app);
        const previousApp = this.getApp();
        if (previousApp.id) {
            this.props.updateApp(previousApp as IApp, app);
        }
        if (this.mounted) {
            this.updateApp(app);
            this.props.history.replace(app.name);
        }
    };

    private onPutFailure = (reason: string, app: IApp): void =>
        super.toast(`Não foi possível atualizar a aplicação ${this.mounted ? `<b class='white-text'>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);

    private onDeleteSuccess = (app: IApp): void => {
        super.toast(`<span class='green-text'>A aplicação<b>${app.name}</b> foi apagada com sucesso</span>`);
        if (this.mounted) {
            this.props.history.push(`/aplicações`);
        }
    };

    private onDeleteFailure = (reason: string, app: IApp): void =>
        super.toast(`Não foi possível apagar a aplicação ${this.mounted ? `<b>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedServices.length
        || !!this.state.unsavedRules.length
        || !!this.state.unsavedSimulatedMetrics.length;

    private saveEntities = (app: IApp) => {
        this.saveAppServices(app);
        this.saveAppRules(app);
        this.saveAppSimulatedMetrics(app);
    };

    private addAppService = (service: IAddAppService): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.concat(service)
        });
    };

    private updateAppService = (service: IAppService | IAddAppService): void => {
        const updatedUnsavedServices = this.state.unsavedServices.filter(s => service.service.serviceName !== s.service.serviceName);
        updatedUnsavedServices.push(service);
        this.setState(({unsavedServices: updatedUnsavedServices}));
    }

    private removeAppServices = (services: string[]): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.filter(service => !services.includes(service.service.serviceName))
        });
    };

    private saveAppServices = (app: IApp): void => {
        const {unsavedServices} = this.state;
        if (unsavedServices.length) {
            postData(`apps/${app.name}/services`, unsavedServices,
                () => this.onSaveServicesSuccess(app),
                (reason) => this.onSaveServicesFailure(app, reason));
        }
    };

    private onSaveServicesSuccess = (app: IApp): void => {
        this.props.addAppServices(app.name, this.state.unsavedServices);
        if (this.mounted) {
            this.setState({unsavedServices: []});
        }
    };

    private onSaveServicesFailure = (app: IApp, reason: string): void =>
        super.toast(`Não foi possível guardar os serviços associados à aplicação ${this.mounted ? `<b>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);

    private addAppRule = (rule: string): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.concat(rule)
        });
    };

    private removeAppRules = (rules: string[]): void => {
        this.setState({
            unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
        });
    };

    private saveAppRules = (app: IApp): void => {
        const {unsavedRules} = this.state;
        if (unsavedRules.length) {
            postData(`apps/${app.name}/rules`, unsavedRules,
                () => this.onSaveRulesSuccess(app),
                (reason) => this.onSaveRulesFailure(app, reason));
        }
    };

    private onSaveRulesSuccess = (app: IApp): void => {
        this.props.addAppRules(app.name, this.state.unsavedRules);
        if (this.mounted) {
            this.setState({unsavedRules: []});
        }
    };

    private onSaveRulesFailure = (app: IApp, reason: string): void =>
        super.toast(`Não foi possível guardar as regras associadas à aplicação ${this.mounted ? `<b>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);

    private addAppSimulatedMetric = (simulatedMetric: string): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
        });
    };

    private removeAppSimulatedMetrics = (simulatedMetrics: string[]): void => {
        this.setState({
            unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
        });
    };

    private saveAppSimulatedMetrics = (app: IApp): void => {
        const {unsavedSimulatedMetrics} = this.state;
        if (unsavedSimulatedMetrics.length) {
            postData(`apps/${app.name}/simulated-metrics`, unsavedSimulatedMetrics,
                () => this.onSaveSimulatedMetricsSuccess(app),
                (reason) => this.onSaveSimulatedMetricsFailure(app, reason));
        }
    };

    private onSaveSimulatedMetricsSuccess = (app: IApp): void => {
        this.props.addAppSimulatedMetrics(app.name, this.state.unsavedSimulatedMetrics);
        if (this.mounted) {
            this.setState({unsavedSimulatedMetrics: []});
        }
    };

    private onSaveSimulatedMetricsFailure = (app: IApp, reason: string): void =>
        super.toast(`Não foi possível guardar as métricas simuladas associadas à aplicação ${this.mounted ? `<b>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);

    private launchButton = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        if (!isNew(this.props.location.search)) {
            buttons.push({
                button:
                    <>
                        <button
                            className={`modal-trigger btn-flat btn-small blue-text ${formStyles.formButton}`}
                            data-for='dark-tooltip' data-tip="Lançar todos os microserviços desta aplicação"
                            data-place={'bottom'}
                            data-target={'launch-app-modal'}>
                            Iniciar
                        </button>
                    </>
            });
        }
        return buttons;
    };

    private launchApp = (position: IMarker) => {
        const app = this.getApp();
        const url = `apps/${app.name}/launch`;
        this.setState({loading: {method: 'post', url: url}});
        postData(url, position,
            (reply: IReply<ILaunchApp>) => this.onLaunchSuccess(reply.data),
            (reason: string) => this.onLaunchFailure(reason, app));
    };

    private onLaunchSuccess = (launchApp: ILaunchApp) => {
        super.toast(`<span><span class='green-text'>Os serviços foram lançados com sucesso:<br/>
        </span>${Object.entries(launchApp)
                .map(([service, containers]) => `<b>${service}</b> ${containers.map(c =>
                    `<a href='/contentores/${c.id}'>${c.id}</a>`).join(', ')}`).join('<br/>')}</span>`,
            20000);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private onLaunchFailure = (reason: string, app: Partial<IApp>) => {
        super.toast(`Não foi possível lançar todos os serviços da aplicação ${this.mounted ? `<b>${app.name}</b>` : `<a href='/aplicações/${app.name}'><b>${app.name}</b></a>`}`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private updateApp = (app: IApp) => {
        app = Object.values(normalize(app, Schemas.APP).entities.apps || {})[0];
        const formApp = {...app};
        removeFields(formApp);
        this.setState({app: app, formApp: formApp, loading: undefined});
    };

    private getFields = (app: Partial<IApp>): IFields =>
        Object.entries(app).map(([key, _]) => {
            return {
                [key]: {
                    id: key,
                    label: key,
                    validation: key === 'description'
                        ? {rule: maxSizeAndTrimmed, args: 1024}
                        : {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});

    private getContainersMarkers = (): IMarker[] => {
        const containers: IContainer[] = Object.values(this.props.containers);
        const markers = new Map<String, IMarker>();
        containers
            .forEach(container => {
                const publicIpAddress = container.publicIpAddress;
                const marker = markers.get(publicIpAddress) || {title: '', label: '', latitude: 0, longitude: 0};
                if (marker.title === '') {
                    marker.title += container.coordinates.label + '<br/>';
                }
                marker.title += container.id.toString().substr(0, 5) + ' - ' + container.labels['serviceName'] + '<br/>';
                marker.label = publicIpAddress;
                marker.latitude = container.coordinates.latitude;
                marker.longitude = container.coordinates.longitude;
                marker.color = '#00FF00';
                markers.set(publicIpAddress, marker);
            });
        return Array.from(markers.values());
    }

    private app = () => {
        const {isLoading, error} = this.props;
        const app = this.getApp();
        const formApp = this.getFormApp();
        // @ts-ignore
        const appKey: (keyof IApp) = formApp && Object.keys(formApp)[0];
        const isNewApp = this.isNew();
        return (
            <>
                {isLoading && <LoadingSpinner/>}
                {!isLoading && error && <Error message={error}/>}
                {!isLoading && !error && formApp && (
                    <>
                        {/*@ts-ignore*/}
                        <Form id={appKey}
                              fields={this.getFields(formApp)}
                              values={app}
                              isNew={isNew(this.props.location.search)}
                              showSaveButton={this.shouldShowSaveButton()}
                              post={{
                                  url: 'apps',
                                  successCallback: this.onPostSuccess,
                                  failureCallback: this.onPostFailure
                              }}
                              put={{
                                  url: `apps/${app.name}`,
                                  successCallback: this.onPutSuccess,
                                  failureCallback: this.onPutFailure
                              }}
                              delete={{
                                  url: `apps/${app.name}`,
                                  successCallback: this.onDeleteSuccess,
                                  failureCallback: this.onDeleteFailure
                              }}
                              customButtons={this.launchButton()}
                              saveEntities={this.saveEntities}
                              loading={this.state.loading}>
                            {Object.keys(formApp).map((key, index) =>
                                key === 'description'
                                    ? <Field key={index}
                                             id={key}
                                             type={'multilinetext'}
                                             label={key}/>
                                    : <Field key={index}
                                             id={key}
                                             label={key}/>
                            )}
                        </Form>
                        <LaunchAppDialog launchAppCallback={this.launchApp} markers={this.getContainersMarkers()}/>
                    </>
                )}
            </>
        )
    };

    private services = (): JSX.Element =>
        <AppServicesList isLoadingApp={this.props.isLoading}
                         loadAppError={this.props.error}
                         app={this.getApp()}
                         unsavedServices={this.state.unsavedServices}
                         onAddAppService={this.addAppService}
                         onRemoveAppServices={this.removeAppServices}
                         updateAppService={this.updateAppService}/>

    private rules = (): JSX.Element =>
        <AppRuleList isLoadingApp={this.props.isLoading}
                     loadAppError={this.props.error}
                     app={this.getApp()}
                     unsavedRules={this.state.unsavedRules}
                     onAddAppRule={this.addAppRule}
                     onRemoveAppRules={this.removeAppRules}/>;

    private genericRules = (): JSX.Element =>
        <GenericServiceRuleList/>;

    private simulatedMetrics = (): JSX.Element =>
        <AppSimulatedMetricList isLoadingApp={this.props.isLoading}
                                loadAppError={this.props.error}
                                app={this.getApp()}
                                unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                onAddSimulatedAppMetric={this.addAppSimulatedMetric}
                                onRemoveSimulatedAppMetrics={this.removeAppSimulatedMetrics}/>;

    private genericSimulatedMetrics = (): JSX.Element =>
        <GenericSimulatedServiceMetricList/>;

    private tabs = (): Tab[] => [
        {
            title: 'Aplicação',
            id: 'app',
            content: () => this.app(),
            active: this.props.location.state?.selected === 'app'
        },
        {
            title: 'Serviços',
            id: 'services',
            content: () => this.services(),
            active: this.props.location.state?.selected === 'services',
        },
        {
            title: 'Regras',
            id: 'appRules',
            content: () => this.rules(),
            active: this.props.location.state?.selected === 'rules'
        },

        {
            title: 'Regras genéricas',
            id: 'genericAppRules',
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
    ];

}

function removeFields(app: Partial<IApp>) {
    delete app["id"];
    delete app["services"];
    delete app["appRules"];
    delete app["appSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.apps.isLoadingApps;
    const error = state.entities.apps.loadAppsError;
    const name = props.match.params.name
    const app = isNew(props.location.search) ? buildNewApp() : state.entities.apps.data[name];
    let formApp;
    if (app) {
        formApp = {...app};
        removeFields(formApp);
    }
    return {
        isLoading,
        error,
        app,
        formApp,
        regions: state.entities.regions.data,
        containers: state.entities.containers.data
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadApps,
    loadRegions,
    loadContainers,
    addApp,
    updateApp,
    addAppServices,
    addAppRules,
    addAppSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(App);
