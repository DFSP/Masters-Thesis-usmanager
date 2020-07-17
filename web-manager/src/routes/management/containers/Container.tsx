import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../components/BaseComponent";
import Form, {
  ICustomButton,
  IFields, IFormLoading,
  requiredAndNumberAndMin,
  requiredAndTrimmed
} from "../../../components/form/Form";
import Field, {getTypeFromValue} from "../../../components/form/Field";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
  addContainer,
  addContainerRules,
  addContainerSimulatedMetrics,
  loadCloudHosts,
  loadContainers,
  loadEdgeHosts,
  loadServices
} from "../../../actions";
import {connect} from "react-redux";
import React, {createRef} from "react";
import {awsInstanceStates, ICloudHost} from "../hosts/cloud/CloudHost";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IService} from "../services/Service";
import ContainerPortsList from "./ContainerPortsList";
import ContainerLabelsList from "./ContainerLabelsList";
import ContainerLogsList from "./ContainerLogsList";
import PerfectScrollbar from "react-perfect-scrollbar";
import ScrollBar from "react-perfect-scrollbar";
import M from "materialize-css";
import styles from "../../../components/list/ControlledList.module.css";
import {decodeHTML} from "../../../utils/text";
import {IReply, postData} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import IDatabaseData from "../../../components/IDatabaseData";
import GenericContainerRuleList from "./GenericContainerRuleList";
import ContainerRuleList from "./ContainerRuleList";
import ContainerSimulatedMetricList from "./ContainerSimulatedMetricList";
import GenericSimulatedContainerMetricList from "./GenericSimulatedContainerMetricList";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import formStyles from "../../../components/form/Form.module.css";

export interface IContainer extends IDatabaseData {
  containerId: string;
  created: number;
  names: string[];
  image: string;
  command: string;
  hostname: string;
  ports: IContainerPort[];
  labels: IContainerLabel;
  logs?: string;
  containerRules?: string[];
  containerSimulatedMetrics?: string[];
}

export interface IContainerPort {
  privatePort: number;
  publicPort: number;
  type: string;
  ip: string;
}

export interface IContainerLabel {
  [key: string]: string
}

interface INewContainer {
  hostname?: string,
  service?: string,
  internalPort?: number,
  externalPort?: number,
}

const buildNewContainer = (): INewContainer => ({
  hostname: undefined,
  service: undefined,
  internalPort: undefined,
  externalPort: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  newContainer?: INewContainer;
  container?: IContainer;
  formContainer?: Partial<IContainer> | INewContainer;
  cloudHosts: { [key: string]: ICloudHost };
  edgeHosts: { [key: string]: IEdgeHost };
  services: { [key: string]: IService };
}

interface DispatchToProps {
  loadContainers: (id: string) => void;
  loadCloudHosts: () => void;
  loadEdgeHosts: () => void;
  loadServices: () => void;
  addContainer: (container: IContainer) => void;
  addContainerRules: (containerId: string, rules: string[]) => void;
  addContainerSimulatedMetrics: (containerId: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
  id: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  container?: IContainer,
  formContainer?: IContainer,
  loading: IFormLoading,
  defaultInternalPort: number,
  defaultExternalPort: number,
  unsavedRules: string[],
  unsavedSimulatedMetrics: string[],
}

class Container extends BaseComponent<Props, State> {

  private mounted = false;
  private replicateDropdown = createRef<HTMLButtonElement>();
  private migrateDropdown = createRef<HTMLButtonElement>();
  private scrollbar: (ScrollBar | null) = null;

  state: State = {
    loading: undefined,
    defaultInternalPort: 0,
    defaultExternalPort: 0,
    unsavedRules: [],
    unsavedSimulatedMetrics: [],
  };

  public componentDidMount(): void {
    this.loadContainer();
    this.props.loadCloudHosts();
    this.props.loadEdgeHosts();
    this.props.loadServices();
    this.mounted = true;
  };

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    this.init();
  }

  private init = () => {
    M.Dropdown.init(this.replicateDropdown.current as Element,
      {
        onOpenEnd: this.onOpenDropdown
      });
    M.Dropdown.init(this.migrateDropdown.current as Element,
      {
        onOpenEnd: this.onOpenDropdown
      });
  };

  private onOpenDropdown = () =>
    this.scrollbar?.updateScroll();

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadContainer = () => {
    if (!isNew(this.props.location.search)) {
      const containerId = this.props.match.params.id;
      this.props.loadContainers(containerId);
    }
  };

  private getContainer = () =>
    this.state.container || this.props.container;

  private getFormContainer = () =>
    this.state.formContainer || this.props.formContainer;

  private isNew = () =>
    isNew(this.props.location.search);

  private onPostSuccess = (reply: IReply<IContainer>): void => {
    const container = reply.data;
    super.toast(`<span class="green-text">Container ${this.mounted ? `<b class="white-text">${container.containerId}</b>` : `<a href=/containers/${container.containerId}><b>${container.containerId}</b></a>`} has started at ${container.hostname}</span>`);
    this.props.addContainer(container);
    this.saveEntities(container);
    if (this.mounted) {
      this.updateContainer(container);
      this.props.history.replace(container.containerId);
    }
  };

  private onPostFailure = (reason: string, container: IContainer): void =>
    super.toast(`Unable to start container at <b>${container.hostname}</b>`, 10000, reason, true);

  private onDeleteSuccess = (container: IContainer): void => {
    super.toast(`<span class="green-text">Container <b class="white-text">${container.containerId}</b> successfully stopped</span>`);
    if (this.mounted) {
      this.props.history.push(`/containers`);
    }
  };

  private onDeleteFailure = (reason: string, container: IContainer): void =>
    super.toast(`Unable to stop ${this.mounted ? `<b>${container.containerId}</b>` : `<a href=/containers/${container.containerId}><b>${container.containerId}</b></a>`} container`, 10000, reason, true);

  private replicateMigrateButtons = (): ICustomButton[] => {
    const buttons: ICustomButton[] = [];
    buttons.push({
        button:
          <>
            <button className={`btn-flat btn-small waves-effect waves-light blue-text dropdown-trigger ${formStyles.formButton}`}
                    data-target={`replicate-dropdown-hostname`}
                    ref={this.replicateDropdown}>
              Replicate
            </button>
            {this.chooseHostnameDropdown('replicate-dropdown-hostname', this.replicate)}
          </>
      },
      {
        button:
          <>
            <button className={`btn-flat btn-small waves-effect waves-light blue-text dropdown-trigger ${formStyles.formButton}`}
                    data-target={`migrate-dropdown-hostname`}
                    ref={this.migrateDropdown}>
              Migrate
            </button>
            {this.chooseHostnameDropdown('migrate-dropdown-hostname', this.migrate)}
          </>
      });
    return buttons;
  };

  private replicate = (event: any) => {
    const container = this.getContainer();
    const hostname = decodeHTML((event.target as HTMLLIElement).innerHTML);
    const url = `containers/${container?.containerId}/replicate`;
    this.setState({ loading: { method: 'post', url: url } });
    postData(url, {hostname: hostname},
      (reply: IReply<IContainer>) => this.onReplicateSuccess(reply.data),
      (reason: string) => this.onReplicateFailure(reason, container));
  };

  private onReplicateSuccess = (container: IContainer) => {
    super.toast(`<span class="green-text">Replicated ${container.image.split('/').splice(1)} to container </span><a href=/containers/${container.containerId}><b>${container.containerId}</b></a>`, 15000);
    if (this.mounted) {
      this.setState({loading: undefined});
    }
  };

  private onReplicateFailure = (reason: string, container?: IContainer) => {
    super.toast(`Unable to replicate ${this.mounted ? `<b>${container?.containerId}</b>` : `<a href=/containers/${container?.containerId}><b>${container?.containerId}</b></a>`} container`, 10000, reason, true);
    if (this.mounted) {
      this.setState({loading: undefined});
    }
  };

  private migrate = (event: any) => {
    const container = this.getContainer();
    const hostname = decodeHTML((event.target as HTMLLIElement).innerHTML);
    const url = `containers/${container?.containerId}/migrate`;
    this.setState({ loading: { method: 'post', url: url } });
    postData(url, { hostname: hostname },
      (reply: IReply<IContainer>) => this.onMigrateSuccess(reply.data),
      (reason) => this.onMigrateFailure(reason, container));
  };

  private onMigrateSuccess = (container: IContainer) => {
    const parentContainer = this.getContainer();
    super.toast(`<span class="green-text">Migrated ${this.mounted ? parentContainer?.containerId : `<a href=/containers/${parentContainer?.containerId}>${parentContainer?.containerId}</a>`} to container </span><a href=/containers/${container.containerId}>${container.containerId}</a>`, 15000);
    if (this.mounted) {
      this.setState({loading: undefined});
    }
  };

  private onMigrateFailure = (reason: string, container?: IContainer) => {
    super.toast(`Unable to migrate ${this.mounted ? `<b>${container?.containerId}</b>` : `<a href=/containers/${container?.containerId}><b>${container?.containerId}</b></a>`} container`, 10000, reason, true);
    if (this.mounted) {
      this.setState({loading: undefined});
    }
  };

  private addContainerRule = (rule: string): void => {
    this.setState({
      unsavedRules: this.state.unsavedRules.concat(rule)
    });
  };

  private removeContainerRules = (rules: string[]): void => {
    this.setState({
      unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
    });
  };

  private saveContainerRules = (container: IContainer): void => {
    const {unsavedRules} = this.state;
    if (unsavedRules.length) {
      postData(`containers/${container.containerId}/rules`, unsavedRules,
        () => this.onSaveRulesSuccess(container),
        (reason) => this.onSaveRulesFailure(container, reason));
    }
  };

  private onSaveRulesSuccess = (container: IContainer): void => {
    this.props.addContainerRules(container.containerId, this.state.unsavedRules);
    if (this.mounted) {
      this.setState({ unsavedRules: [] });
    }
  };

  private onSaveRulesFailure = (container: IContainer, reason: string): void =>
    super.toast(`Unable to save rules of ${this.mounted ? `<b>${container.containerId}</b>` : `<a href=/containers/${container.containerId}><b>${container.containerId}</b></a>`} container`, 10000, reason, true);

  private addContainerSimulatedMetric = (simulatedMetric: string): void => {
    this.setState({
      unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
    });
  };

  private removeContainerSimulatedMetrics = (simulatedMetrics: string[]): void => {
    this.setState({
      unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
    });
  };

  private saveContainerSimulatedMetrics = (container: IContainer): void => {
    const {unsavedSimulatedMetrics} = this.state;
    if (unsavedSimulatedMetrics.length) {
      postData(`containers/${container.containerId}/simulated-metrics`, unsavedSimulatedMetrics,
        () => this.onSaveSimulatedMetricsSuccess(container),
        (reason) => this.onSaveSimulatedMetricsFailure(container, reason));
    }
  };

  private onSaveSimulatedMetricsSuccess = (container: IContainer): void => {
    this.props.addContainerSimulatedMetrics(container.containerId, this.state.unsavedSimulatedMetrics);
    if (this.mounted) {
      this.setState({ unsavedSimulatedMetrics: [] });
    }
  };

  private onSaveSimulatedMetricsFailure = (container: IContainer, reason: string): void =>
    super.toast(`Unable to save simulated metrics of ${this.mounted ? `<b>${container.containerId}</b>` : `<a href=/containers/${container.containerId}><b>${container.containerId}</b></a>`} container`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedRules.length
    || !!this.state.unsavedSimulatedMetrics.length;

  private saveEntities = (container: IContainer) => {
    this.saveContainerRules(container);
    this.saveContainerSimulatedMetrics(container);
  };

  private chooseHostnameDropdown = (id: string, onClick: (event: any) => void) =>
    <ul id={id}
        className={`dropdown-content ${styles.dropdown}`}>
      <li className={`${styles.disabled}`}>
        <a>
          Choose hostname
        </a>
      </li>
      <PerfectScrollbar ref={(ref) => { this.scrollbar = ref; }}>
        {Object.values(this.props.cloudHosts).map((data, index) =>
          data.publicIpAddress &&
          <li key={index} onClick={onClick}>
              <a>
                {data.publicIpAddress}
              </a>
          </li>
        )}
        {Object.values(this.props.edgeHosts).map((data, index) =>
          <li key={index} onClick={onClick}>
            <a>
              {data.publicDnsName || data.publicIpAddress}
            </a>
          </li>
        )}
      </PerfectScrollbar>
    </ul>;

  private updateContainer = (container: IContainer) => {
    container = Object.values(normalize(container, Schemas.CONTAINER).entities.containers || {})[0];
    const formContainer = { ...container };
    removeFields(formContainer);
    this.setState({container: container, formContainer: formContainer, loading: undefined});
  };

  private getFields = (container: Partial<IContainer> | INewContainer): IFields =>
    Object.entries(container).map(([key, value]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation: getTypeFromValue(value) === 'number'
            ? { rule: requiredAndNumberAndMin, args: 0 }
            : { rule: requiredAndTrimmed }
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private getSelectableHosts = () => {
    const cloudHosts = Object.values(this.props.cloudHosts)
                             .filter(instance => instance.state.code === awsInstanceStates.RUNNING.code)
                             .map(instance => instance.publicIpAddress);
    const edgeHosts = Object.keys(this.props.edgeHosts);
    return cloudHosts.concat(edgeHosts);
  };

  //TODO get apps' services instead (in case a service is associated to more than 1 app)
  private getSelectableServices = () =>
    Object.keys(this.props.services);

  private setDefaultPorts = (serviceName: string) => {
    const service = this.props.services[serviceName];
    this.setState({
      defaultExternalPort: service.defaultExternalPort,
      defaultInternalPort: service.defaultInternalPort});
  };

  private formFields = (formContainer: Partial<IContainer>, isNew: boolean): JSX.Element =>
    isNew ?
      <>
        <Field key={'hostname'}
               id={'hostname'}
               label={'hostname'}
               type={'dropdown'}
               dropdown={{
                 defaultValue: "Select hostname",
                 values: this.getSelectableHosts()}}/>
        <Field key={'service'}
               id={'service'}
               label={'service'}
               type={'dropdown'}
               dropdown={{
                 defaultValue: "Select service",
                 values: this.getSelectableServices(),
                 selectCallback: this.setDefaultPorts}}/>
        <Field key={'internalPort'}
               id={'internalPort'}
               label={'internalPort'}
               type={'number'}/>
        <Field key={'externalPort'}
               id={'externalPort'}
               label={'externalPort'}
               type={'number'}/>
      </>
      :
      <>
        {Object.entries(formContainer).map(([key, value], index) => {
          return key === 'created'
            ? <Field key={index}
                     id={key}
                     label={key}
                     type={"date"}/>
            : <Field key={index}
                     id={key}
                     label={key}/>
        })}
      </>;

  private container = () => {
    const {isLoading, error, newContainer} = this.props;
    const container = this.getContainer();
    const formContainer = this.getFormContainer();
    const isNewContainer = this.isNew();
    const containerValues = isNewContainer
      ? {
        ...newContainer,
        internalPort: this.state.defaultInternalPort,
        externalPort: this.state.defaultExternalPort
      }
      : formContainer;
    // @ts-ignore
    const containerKey: (keyof IContainer) = formContainer && Object.keys(formContainer)[0];
    return (
      <>
        {!isNewContainer && isLoading && <ListLoadingSpinner/>}
        {!isNewContainer && !isLoading && error && <Error message={error}/>}
        {(isNewContainer || !isLoading) && (isNewContainer || !error) && containerValues && (
          <Form id={containerKey}
                fields={this.getFields(formContainer || {})}
                values={containerValues}
                isNew={isNewContainer}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  textButton: 'Launch',
                  url: 'containers',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                delete={container && (!container.labels['isStoppable'] || container.labels['isStoppable'] === 'true')
                  ? {textButton: 'Stop',
                    url: `containers/${container.containerId}`,
                    successCallback: this.onDeleteSuccess,
                    failureCallback: this.onDeleteFailure}
                  : undefined}
                customButtons={container && (!container.labels['isReplicable'] || container.labels['isReplicable'] === 'true')
                  ? this.replicateMigrateButtons()
                  : undefined}
                loading={this.state.loading}
                saveEntities={this.saveEntities}>
            {this.formFields(formContainer || {}, isNewContainer)}
          </Form>
        )}
      </>
    )
  };

  private ports = (): JSX.Element =>
    <ContainerPortsList isLoadingContainer={this.props.isLoading}
                        loadContainerError={!this.isNew() ? this.props.error : undefined}
                        container={this.getContainer()}/>;

  private labels = (): JSX.Element =>
    <ContainerLabelsList isLoadingContainer={this.props.isLoading}
                         loadContainerError={!this.isNew() ? this.props.error : undefined}
                         container={this.getContainer()}/>;

  private logs = (): JSX.Element =>
    <ContainerLogsList isLoadingContainer={this.props.isLoading}
                       loadContainerError={!this.isNew() ? this.props.error : undefined}
                       container={this.getContainer()}/>;

  private rules = (): JSX.Element =>
    <ContainerRuleList isLoadingContainer={this.props.isLoading}
                       loadContainerError={!this.isNew() ? this.props.error : undefined}
                       container={this.getContainer()}
                       unsavedRules={this.state.unsavedRules}
                       onAddContainerRule={this.addContainerRule}
                       onRemoveContainerRules={this.removeContainerRules}/>;

  private genericRules = (): JSX.Element =>
    <GenericContainerRuleList/>;

  private simulatedMetrics = (): JSX.Element =>
    <ContainerSimulatedMetricList isLoadingContainer={this.props.isLoading}
                                  loadContainerError={!this.isNew() ? this.props.error : undefined}
                                  container={this.getContainer()}
                                  unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                  onAddSimulatedContainerMetric={this.addContainerSimulatedMetric}
                                  onRemoveSimulatedContainerMetrics={this.removeContainerSimulatedMetrics}/>;

  private genericSimulatedMetrics = (): JSX.Element =>
    <GenericSimulatedContainerMetricList/>;

  private tabs = (): Tab[] => ([
    {
      title: 'Container',
      id: 'container',
      content: () => this.container()
    },
    {
      title: 'Ports',
      id: 'ports',
      content: () => this.ports(),
      hidden: this.isNew()
    },
    {
      title: 'Labels',
      id: 'containerLabels',
      content: () => this.labels(),
      hidden: this.isNew()
    },
    {
      title: 'Logs',
      id: 'logs',
      content: () => this.logs(),
      hidden: this.isNew()
    },
    {
      title: 'Rules',
      id: 'rules',
      content: () => this.rules()
    },
    {
      title: 'Generic rules',
      id: 'genericEdgeRules',
      content: () => this.genericRules()
    },
    {
      title: 'Simulated metrics',
      id: 'simulatedMetrics',
      content: () => this.simulatedMetrics()
    },
    {
      title: 'Generic simulated metrics',
      id: 'genericSimulatedMetrics',
      content: () => this.genericSimulatedMetrics()
    }
  ]);

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

}

function removeFields(container: Partial<IContainer>) {
  delete container["id"];
  delete container["ports"];
  delete container["labels"];
  delete container["logs"];
  delete container["containerRules"];
  delete container["containerSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.containers.isLoadingContainers;
  const error = state.entities.containers.loadContainersError;
  const id = props.match.params.id;
  const newContainer = isNew(props.location.search) ? buildNewContainer() : undefined;
  const container = !isNew(props.location.search) ? state.entities.containers.data[id] : undefined;
  let formContainer;
  if (newContainer) {
    formContainer = { ...newContainer };
  }
  if (container) {
    formContainer = { ...container };
    removeFields(formContainer);
  }
  const cloudHosts = state.entities.hosts.cloud.data;
  const edgeHosts = state.entities.hosts.edge.data;
  const services = state.entities.services.data;
  return  {
    isLoading,
    error,
    newContainer,
    container,
    formContainer,
    cloudHosts,
    edgeHosts,
    services
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadContainers,
  addContainer,
  loadCloudHosts,
  loadEdgeHosts,
  loadServices,
  addContainerRules,
  addContainerSimulatedMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(Container);