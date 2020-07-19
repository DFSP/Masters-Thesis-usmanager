import BaseComponent from "../../../components/BaseComponent";
import React from "react";
import ListItem from "../../../components/list/ListItem";
import listItemStyles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadAppServices, loadServices, removeAppServices,} from "../../../actions";
import {connect} from "react-redux";
import {IApp} from "./App";
import {IService} from "../services/Service";
import Field from "../../../components/form/Field";
import {IFields, IValues, requiredAndNumberAndMinAndMax} from "../../../components/form/Form";
import IDatabaseData from "../../../components/IDatabaseData";

export interface IAppService extends IDatabaseData {
  service: IService;
  launchOrder: number;
}

export interface IAddAppService {
  service: { serviceName: string };
  launchOrder: number;
}

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  services: { [key: string]: IService };
  appServices: IAppService[];
}

interface DispatchToProps {
  loadServices: () => void;
  loadAppServices: (appName: string) => void;
  removeAppServices: (appName: string, services: string[]) => void;
}

interface ServiceAppListProps {
  isLoadingApp: boolean;
  loadAppError?: string | null;
  app: IApp | Partial<IApp> | null;
  unsavedServices: IAddAppService[];
  onAddAppService: (service: IAddAppService) => void;
  onRemoveAppServices: (services: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceAppListProps;

interface State {
  selectedService?: string;
  entitySaved: boolean;
}

class AppServiceList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {entitySaved: !this.isNew()};
  }

  public componentDidMount(): void {
    this.props.loadServices();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (!prevProps.app?.id && this.props.app?.id) {
      this.setState({entitySaved: true});
    }
  }

  public render() {
    const isNew = this.isNew();
    return <ControlledList<IAppService> isLoading={!isNew ? this.props.isLoadingApp || this.props.isLoading : undefined}
                                        error={!isNew ? this.props.loadAppError || this.props.error : undefined}
                                        emptyMessage={`Services list is empty`}
                                        data={this.props.appServices}
                                        dataKey={['service', 'serviceName']}
                                        dropdown={{
                                          id: 'appServices',
                                          title: 'Add service',
                                          empty: 'No more services to add',
                                          data: this.getSelectableServicesNames(),
                                          onSelect: this.onDropdownSelect,
                                          formModal: {
                                            id: 'appService',
                                            fields: this.getModalFields(),
                                            values: this.getModalValues(),
                                            content: this.addModal,
                                            position: '20%',
                                          }
                                        }}
                                        show={this.service}
                                        onAddInput={this.onAdd}
                                        onRemove={this.onRemove}
                                        onDelete={{
                                          url: `apps/${this.props.app?.name}/services`,
                                          successCallback: this.onDeleteSuccess,
                                          failureCallback: this.onDeleteFailure
                                        }}
                                        entitySaved={this.state.entitySaved}
                                        sort={(a: IAppService, b: IAppService) => a.launchOrder - b.launchOrder}/>;
  }

  private loadEntities = () => {
    if (this.props.app?.name) {
      const {name} = this.props.app;
      this.props.loadAppServices(name);
    }
  };

  private isNew = () =>
    this.props.app?.name === undefined;

  private service = (index: number, service: IAppService | IAddAppService, separate: boolean, checked: boolean,
                     handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const serviceName = service.service.serviceName;
    const isNew = this.isNew();
    const unsaved = this.props.unsavedServices.map(service => service.service.serviceName).includes(serviceName);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${listItemStyles.linkedItemContent}`}>
          <label>
            <input id={serviceName}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? listItemStyles.unsavedItem : undefined}>
                {service.launchOrder}. {serviceName}
              </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/services/${serviceName}`}
                className={`${listItemStyles.link} waves-effect`}>
            <i className={`${listItemStyles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (service: IValues): void => {
    console.log(service)
    this.props.onAddAppService(service as IAddAppService);
    this.setState({selectedService: undefined});
  };

  private onRemove = (services: string[]): void => {
    this.props.onRemoveAppServices(services);
  };

  private onDeleteSuccess = (services: string[]): void => {
    if (this.props.app?.name) {
      const {name} = this.props.app;
      this.props.removeAppServices(name, services);
    }
  };

  private onDeleteFailure = (reason: string, services: string[]): void =>
    super.toast(`Unable to remove ${services.length === 1 ? services[0] : 'services'} from <b>${this.props.app?.name}</b> app`, 10000, reason, true);

  private getSelectableServicesNames = () => {
    const {appServices, services, unsavedServices} = this.props;
    const nonSystemServices = Object.entries(services)
                                    .filter(([_, value]) => value.serviceType.toLowerCase() !== 'system')
                                    .map(([key, _]) => key);
    const serviceNames = appServices.map(appService => appService.service.serviceName);
    const unsavedServicesNames = unsavedServices.map(service => service.service.serviceName);
    return nonSystemServices.filter(name => !serviceNames.includes(name) && !unsavedServicesNames.includes(name));
  };

  private addModal = () =>
    <>
      <Field key='launchOrder' id={'launchOrder'} label='launchOrder' type={'number'}
             number={{min: 0, max: 2147483647}}/>
    </>;

  private getModalFields = (): IFields => (
    {
      launchOrder: {
        id: 'launchOrder',
        label: 'launchOrder',
        validation: {rule: requiredAndNumberAndMinAndMax, args: [0, 2147483647]}
      }
    }
  );

  private getModalValues = (): IValues => (
    {
      launchOrder: 0
    }
  );

  private onDropdownSelect = (selectedService: string): void => {
    this.setState({selectedService: selectedService});
  };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceAppListProps): StateToProps {
  const appName = ownProps.app?.name;
  const app = appName && state.entities.apps.data[appName];
  const appServices = app && app.services;
  return {
    isLoading: state.entities.apps.isLoadingServices,
    error: state.entities.apps.loadServicesError,
    services: state.entities.services.data,
    appServices: (appServices && Object.values(appServices)) || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadServices,
    loadAppServices,
    removeAppServices,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(AppServiceList);