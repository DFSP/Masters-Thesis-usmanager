/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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
import Form, {
  IFields,
  requiredAndNumberAndMinAndMax,
  requiredAndTrimmed
} from "../../components/form/Form"
import IDatabaseData from "../../components/IDatabaseData";
import {
  addService,
  addServiceApps,
  addServiceDependencies,
  addServicePredictions,
  addServiceRules,
  loadServices
} from "../../actions";
import {connect} from "react-redux";
import MainLayout from "../../views/mainLayout/MainLayout";
import ListLoadingSpinner from "../../components/list/ListLoadingSpinner";
import {ReduxState} from "../../reducers";
import Field, {getTypeFromValue} from "../../components/form/Field";
import BaseComponent from "../../components/BaseComponent";
import Error from "../../components/errors/Error";
import Tabs, {Tab} from "../../components/tabs/Tabs";
import {IReply, postData} from "../../utils/api";
import ServiceAppList, {IAddServiceApp} from "./ServiceAppList";
import ServiceDependencyList from "./ServiceDependencyList";
import ServiceDependeeList from "./ServiceDependeeList";
import ServicePredictionList, {IPrediction} from "./ServicePredictionList";
import ServiceRuleList from "./ServiceRuleList";
import UnsavedChanged from "../../components/form/UnsavedChanges";
import GenericServiceRuleList from "./GenericServiceRuleList";
import {isNew} from "../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../middleware/api";

export interface IService extends IDatabaseData {
  serviceName: string;
  dockerRepository: string;
  defaultExternalPort: number;
  defaultInternalPort: number;
  defaultDb: string;
  launchCommand: string;
  minReplicas: number;
  maxReplicas: number;
  outputLabel: string;
  serviceType: string;
  expectedMemoryConsumption: number;
  apps?: string[];
  dependencies?: string[];
  dependees?: string[];
  predictions?: { [key: string]: IPrediction };
  serviceRules?: string[];
}

const buildNewService = (): Partial<IService> => ({
  serviceName: '',
  dockerRepository: '',
  defaultExternalPort: undefined,
  defaultInternalPort: undefined,
  defaultDb: '',
  launchCommand: '',
  minReplicas: undefined,
  maxReplicas: undefined,
  outputLabel: '',
  serviceType: '',
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
  //TODO updateService: (previousService: Partial<IService>, service: IService) => void;
  addServiceApps: (serviceName: string, apps: string[]) => void;
  addServiceDependencies: (serviceName: string, dependencies: string[]) => void;
  addServicePredictions: (serviceName: string, predictions: IPrediction[]) => void;
  addServiceRules: (serviceName: string, rules: string[]) => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  service?: IService,
  formService?: IService,
  unsavedApps: IAddServiceApp[],
  unsavedDependencies: string[],
  unsavedDependees: string[],
  unsavedPredictions: IPrediction[],
  unsavedRules: string[],
  serviceName?: string,
}

class Service extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
    unsavedApps: [],
    unsavedDependencies: [],
    unsavedDependees: [],
    unsavedPredictions: [],
    unsavedRules: [],
  };

  componentDidMount(): void {
    this.loadService();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadService = () => {
    if (!isNew(this.props.location.search)) {
      const serviceName = this.props.match.params.name;
      this.props.loadServices(serviceName);
    }
  };

  private getService = () =>
    this.state.service || this.props.service;

  private getFormService = () =>
    this.state.formService || this.props.formService;

  private onPostSuccess = (reply: IReply<IService>): void => {
    const service = reply.data;
    super.toast(`<span class="green-text">Service ${this.mounted ? `<b class="white-text">${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} saved</span>`);
    this.props.addService(service);
    this.saveEntities(service);
    if (this.mounted) {
      this.updateService(service);
      this.props.history.replace(service.serviceName);
    }
  };

  private onPostFailure = (reason: string, service: IService): void =>
    super.toast(`Unable to save <b>${service.serviceName}</b> service`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IService>): void => {
    const service = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service have been saved</span>`);
    this.saveEntities(service);
    if (this.mounted) {
      this.updateService(service);
      this.props.history.replace(service.serviceName);
    }
  };

  private onPutFailure = (reason: string, service: IService): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

  private onDeleteSuccess = (service: IService): void => {
    super.toast(`<span class="green-text">Service <b class={'white-text'}>${service.serviceName}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/services`);
    }
  };

  private onDeleteFailure = (reason: string, service: IService): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedApps.length
    || !!this.state.unsavedDependencies.length
    || !!this.state.unsavedDependees.length
    || !!this.state.unsavedPredictions.length
    || !!this.state.unsavedRules.length;

  private saveEntities = (service: IService) => {
    this.saveServiceApps(service);
    this.saveServiceDependencies(service);
    this.saveServicePredictions(service);
    this.saveServiceRules(service);
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
      this.setState({ unsavedApps: [] });
    }
  };

  private onSaveAppsFailure = (service: IService, reason: string): void =>
    super.toast(`Unable to save apps of ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

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
      this.setState({ unsavedDependencies: [] });
    }
  };

  private onSaveDependenciesFailure = (service: IService, reason: string): void =>
    super.toast(`Unable to save dependencies of ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

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
      this.setState({ unsavedPredictions: [] });
    }
  };

  private onSavePredictionsFailure = (service: IService, reason: string): void =>
    super.toast(`Unable to save predictions of ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

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
      this.setState({ unsavedRules: [] });
    }
  };

  private onSaveRulesFailure = (service: IService, reason: string): void =>
    super.toast(`Unable to save rules of ${this.mounted ? `<b>${service.serviceName}</b>` : `<a href=/services/${service.serviceName}><b>${service.serviceName}</b></a>`} service`, 10000, reason, true);

  private updateService = (service: IService) => {
    //const previousService = this.getService();
    service = Object.values(normalize(service, Schemas.SERVICE).entities.services || {})[0];
    //TODO this.props.updateService(previousService, service);
    const formService = { ...service };
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
              ? { rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647] }
              : { rule: requiredAndTrimmed }
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
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formService && (
          <Form id={serviceKey}
                fields={this.getFields(formService)}
                values={service}
                isNew={isNew(this.props.location.search)}
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
                         type="dropdown"
                         label={key}
                         dropdown={{
                           defaultValue: "Choose service type",
                           values: ["Frontend", "Backend", "Database", "System"]}}/>
                : <Field key={index}
                         id={key}
                         label={key}
                         type={value !== undefined ? getTypeFromValue(value) : 'number'}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private apps = (): JSX.Element =>
    <ServiceAppList isLoadingService={this.props.isLoading}
                    loadServiceError={this.props.error}
                    service={this.props.service}
                    newApps={this.state.unsavedApps}
                    onAddServiceApp={this.addServiceApp}
                    onRemoveServiceApps={this.removeServiceApps}/>;

  private dependencies = (): JSX.Element =>
    <ServiceDependencyList isLoadingService={this.props.isLoading}
                           loadServiceError={this.props.error}
                           service={this.props.service}
                           newDependencies={this.state.unsavedDependencies}
                           onAddServiceDependency={this.addServiceDependency}
                           onRemoveServiceDependencies={this.removeServiceDependencies}/>;

  private dependees = (): JSX.Element =>
    <ServiceDependeeList isLoadingService={this.props.isLoading}
                         loadServiceError={this.props.error}
                         service={this.props.service}
                         newDependees={this.state.unsavedDependees}/>;

  private predictions = (): JSX.Element =>
    <ServicePredictionList isLoadingService={this.props.isLoading}
                           loadServiceError={this.props.error}
                           service={this.props.service}
                           newPredictions={this.state.unsavedPredictions}
                           onAddServicePrediction={this.addServicePrediction}
                           onRemoveServicePredictions={this.removeServicePredictions}/>;

  private rules = (): JSX.Element =>
    <ServiceRuleList isLoadingService={this.props.isLoading}
                     loadServiceError={this.props.error}
                     service={this.props.service}
                     newRules={this.state.unsavedRules}
                     onAddServiceRule={this.addServiceRule}
                     onRemoveServiceRules={this.removeServiceRules}/>;

  private genericRules = (): JSX.Element =>
    <GenericServiceRuleList/>;

  private tabs = () => [
    {
      title: 'Service',
      id: 'service',
      content: () => this.service()
    },
    {
      title: 'Apps',
      id: 'apps',
      content: () => this.apps()
    },
    {
      title: 'Dependencies',
      id: 'dependencies',
      content: () => this.dependencies()
    },
    {
      title: 'Dependees',
      id: 'dependees',
      content: () => this.dependees()
    },
    {
      title: 'Predictions',
      id: 'predictions',
      content: () => this.predictions()
    },
    {
      title: 'Rules',
      id: 'serviceRules',
      content: () => this.rules()
    },
    {
      title: 'Generic rules',
      id: 'genericRules',
      content: () => this.genericRules()
    }
  ];

  render() {
    return (
      <MainLayout>
        {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs()}/>
        </div>
      </MainLayout>
    );
  }

}

function removeFields(service: Partial<IService>) {
  delete service["id"];
  delete service["apps"];
  delete service["dependencies"];
  delete service["dependees"];
  delete service["predictions"];
  delete service["serviceRules"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.services.isLoadingServices;
  const error = state.entities.services.loadServicesError;
  const name = props.match.params.name;
  const service = isNew(props.location.search) ? buildNewService() : state.entities.services.data[name];
  let formService;
  if (service) {
    formService = { ...service };
    removeFields(formService);
  }
  return  {
    isLoading,
    error,
    service,
    formService,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadServices,
  addService,
  addServiceApps,
  addServiceDependencies,
  addServicePredictions,
  addServiceRules,
};

export default connect(mapStateToProps, mapDispatchToProps)(Service);
