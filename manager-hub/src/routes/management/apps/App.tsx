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

import IDatabaseData from "../../../components/IDatabaseData";
import BaseComponent from "../../../components/BaseComponent";
import {RouteComponentProps} from "react-router";
import Form, {ICustomButton, IFields, IFormLoading, requiredAndTrimmed} from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import React from "react";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addApp, addAppServices, loadApps, loadRegions, updateApp} from "../../../actions";
import {connect} from "react-redux";
import AppServicesList, {IAddAppService, IAppService} from "./AppServicesList";
import {IReply, postData} from "../../../utils/api";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {isNew} from "../../../utils/router";
import {IRegion} from "../region/Region";
import formStyles from "../../../components/form/Form.module.css";
import {IContainer} from "../containers/Container";
import LaunchAppDialog from "./LaunchAppDialog";
import {Point} from "react-simple-maps";
import {IMarker} from "../../../components/map/Marker";

export interface IApp extends IDatabaseData {
    name: string;
    services?: { [key: string]: IAppService }
}

const buildNewApp = (): Partial<IApp> => ({
    name: undefined,
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
}

interface DispatchToProps {
    loadApps: (name: string) => void;
    loadRegions: () => void;
    addApp: (app: IApp) => void;
    updateApp: (previousApp: IApp, currentApp: IApp) => void;
    addAppServices: (appName: string, appServices: IAddAppService[]) => void;
}

interface MatchParams {
    name: string;
}

interface LocationState {
    data: IApp,
    selected: 'app' | 'services';
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams, {}, LocationState>;

interface State {
    app?: IApp,
    formApp?: IApp,
    unsavedServices: IAddAppService[],
    loading: IFormLoading,
}

class App extends BaseComponent<Props, State> {

    state: State = {
        unsavedServices: [],
        loading: undefined,
    };
    private mounted = false;

    public componentDidMount(): void {
        this.props.loadRegions();
        this.loadApp();
        this.mounted = true;
    };

    componentWillUnmount(): void {
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

    private loadApp = () => {
        if (!isNew(this.props.location.search)) {
            const appName = this.props.match.params.name;
            this.props.loadApps(appName);
        }
    };

    private getApp = () =>
        this.state.app || this.props.app;

    private getFormApp = () =>
        this.state.formApp || this.props.formApp;

    private isNew = () =>
        isNew(this.props.location.search);

    private onPostSuccess = (reply: IReply<IApp>): void => {
        const app = reply.data;
        super.toast(`<span class="green-text">App ${this.mounted ? `<b class="white-text">${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} saved</span>`);
        this.props.addApp(app);
        this.saveEntities(app);
        if (this.mounted) {
            this.updateApp(app);
            this.props.history.replace(app.name);
        }
    };

    private onPostFailure = (reason: string, app: IApp): void =>
        super.toast(`Unable to save <b>${app.name}</b> app`, 10000, reason, true);

    private onPutSuccess = (reply: IReply<IApp>): void => {
        const app = reply.data;
        super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} app have been saved</span>`);
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
        super.toast(`Unable to update ${this.mounted ? `<b class="white-text">${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} app`, 10000, reason, true);

    private onDeleteSuccess = (app: IApp): void => {
        super.toast(`<span class="green-text">App <b class="white-text">${app.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.props.history.push(`/apps`);
        }
    };

    private onDeleteFailure = (reason: string, app: IApp): void =>
        super.toast(`Unable to delete ${this.mounted ? `<b>${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} app`, 10000, reason, true);

    private shouldShowSaveButton = () =>
        !!this.state.unsavedServices.length;

    private saveEntities = (app: IApp) => {
        this.saveAppServices(app);
    };

    private addAppService = (service: IAddAppService): void => {
        this.setState({
            unsavedServices: this.state.unsavedServices.concat(service)
        });
    };

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
        super.toast(`Unable to save services of ${this.mounted ? `<b>${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} app`, 10000, reason, true);


    private launchButton = (): ICustomButton[] => {
        const buttons: ICustomButton[] = [];
        if (!isNew(this.props.location.search)) {
            buttons.push({
                button:
                    <>
                        <button
                            className={`modal-trigger btn-flat btn-small waves-effect waves-light blue-text ${formStyles.formButton}`}
                            data-target={'launch-app-modal'}>
                            Launch
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
        super.toast(`<span><span class="green-text">Successfully launched services<br/>
        </span>${Object.entries(launchApp)
                .map(([service, containers]) => `<b>${service}</b> ${containers.map(c =>
                    `<a href=/containers/${c.containerId}>${c.containerId}</a>`).join(', ')}`).join('<br/>')}</span>`,
            20000);
        if (this.mounted) {
            this.setState({loading: undefined});
        }
    };

    private onLaunchFailure = (reason: string, app: Partial<IApp>) => {
        super.toast(`Failed to launch services of 
        ${this.mounted ? `<b>${app.name}</b>` : `<a href=/apps/${app.name}><b>${app.name}</b></a>`} app`,
            10000, reason, true);
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
                    validation: {rule: requiredAndTrimmed}
                }
            };
        }).reduce((fields, field) => {
            for (let key in field) {
                fields[key] = field[key];
            }
            return fields;
        }, {});


    private app = () => {
        const {isLoading, error} = this.props;
        const app = this.getApp();
        const formApp = this.getFormApp();
        // @ts-ignore
        const appKey: (keyof IApp) = formApp && Object.keys(formApp)[0];
        const isNewApp = this.isNew();
        return (
            <>
                {!isNewApp && isLoading && <ListLoadingSpinner/>}
                {!isNewApp && !isLoading && error && <Error message={error}/>}
                {(isNewApp || !isLoading) && (isNewApp || !error) && formApp && (
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
                                <Field key={index}
                                       id={key}
                                       label={key}/>
                            )}
                        </Form>
                        <LaunchAppDialog launchAppCallback={this.launchApp}/>
                    </>
                )}
            </>
        )
    };

    private services = (): JSX.Element =>
        <AppServicesList isLoadingApp={this.props.isLoading}
                         loadAppError={!this.isNew() ? this.props.error : undefined}
                         app={this.getApp()}
                         unsavedServices={this.state.unsavedServices}
                         onAddAppService={this.addAppService}
                         onRemoveAppServices={this.removeAppServices}/>;

    private tabs = (): Tab[] => [
        {
            title: 'App',
            id: 'app',
            content: () => this.app(),
            active: this.props.location.state?.selected === 'app'
        },
        {
            title: 'Services',
            id: 'services',
            content: () => this.services(),
            active: this.props.location.state?.selected === 'services',
        },
    ];

}

function removeFields(app: Partial<IApp>) {
    delete app["id"];
    delete app["services"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
    const isLoading = state.entities.apps.isLoadingApps;
    const error = state.entities.apps.loadAppsError;
    const name = props.match.params.name;
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
        regions: state.entities.regions.data
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadApps,
    loadRegions,
    addApp,
    updateApp,
    addAppServices
};

export default connect(mapStateToProps, mapDispatchToProps)(App);
