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

import BaseComponent from "../../../components/BaseComponent";
import React, {createRef} from "react";
import {IService} from "./Service";
import ListItem from "../../../components/list/ListItem";
import listItemStyles from "../../../components/list/ListItem.module.css";
import appServiceStyles from "./ServiceAppList.module.css";
import {Link} from "react-router-dom";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadApps, loadAppServices, loadServiceApps, removeServiceApps} from "../../../actions";
import Field from "../../../components/form/Field";
import {IFields, IValues, requiredAndNumberAndMinAndMax} from "../../../components/form/Form";
import List from "../../../components/list/List";
import M from "materialize-css";
import Collapsible from "../../../components/collapsible/Collapsible";
import {IApp} from "../apps/App";
import {IAppService} from "../apps/AppServicesList";
import {connect} from "react-redux";
import ScrollBar from "react-perfect-scrollbar";

export interface IAddServiceApp {
  name: string;
  launchOrder: number;
}

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  apps: { [key: string]: IApp },
  serviceApps: string[];
  isLoadingAppServices: boolean;
}

interface DispatchToProps {
  loadApps: (name?: string) => any;
  loadServiceApps: (serviceName: string) => void;
  loadAppServices: (appName: string) => void;
  removeServiceApps: (serviceName: string, apps: string[]) => void
}

interface ServiceAppListProps {
  isLoadingService: boolean;
  loadServiceError?: string | null;
  service: IService | Partial<IService> | null;
  unsavedApps: IAddServiceApp[];
  onAddServiceApp: (app: IAddServiceApp) => void;
  onRemoveServiceApps: (apps: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceAppListProps;

type State = {
  selectedApp?: string,
  entitySaved: boolean;
}

class ServiceAppList extends BaseComponent<Props, State> {

  private collapsible = createRef<HTMLUListElement>();
  private scrollbar = createRef<ScrollBar>();

  constructor(props: Props) {
    super(props);
    this.state = {entitySaved: !this.isNew()};
  }

  public componentDidMount(): void {
    this.props.loadApps();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    M.Collapsible.init(this.collapsible.current as Element);
    if (!prevProps.service?.serviceName && this.props.service?.serviceName) {
      this.setState({entitySaved: true});
    }
  }

  public render() {
    const isNew = this.isNew();
    return <ControlledList<string>
      isLoading={!isNew ? this.props.isLoadingService || this.props.isLoading : undefined}
      error={!isNew ? this.props.loadServiceError || this.props.error : undefined}
      emptyMessage='Apps list is empty'
      data={this.props.serviceApps}
      dataKey={['name']}
      dropdown={{
        id: 'apps',
        title: 'Add app',
        empty: 'No more apps to add',
        data: this.getSelectableAppsNames(),
        onSelect: this.onSelectApp,
        formModal: {
          id: 'serviceApp',
          title: 'Add app',
          fields: this.getModalFields(),
          values: this.getModalValues(),
          content: this.serviceAppModal,
          position: '20%',
          scrollbar: this.scrollbar,
        }
      }}
      show={this.app}
      onAddInput={this.onAdd}
      onRemove={this.onRemove}
      onDelete={{
        url: `services/${this.props.service?.serviceName}/apps`,
        successCallback: this.onDeleteSuccess,
        failureCallback: this.onDeleteFailure
      }}
      entitySaved={this.state.entitySaved}/>;

  }

  private loadEntities = () => {
    if (this.props.service?.serviceName) {
      const {serviceName} = this.props.service;
      this.props.loadServiceApps(serviceName);
    }
  };

  private isNew = () =>
    this.props.service?.serviceName === undefined;

  private app = (index: number, app: string | IAddServiceApp, separate: boolean, checked: boolean,
                 handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const appName = typeof app === 'string' ? app : app.name;
    const isNew = this.isNew();
    const unsaved = this.props.unsavedApps.map(app => app.name).includes(appName);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${listItemStyles.linkedItemContent}`}>
          <label>
            <input id={appName}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? listItemStyles.unsavedItem : undefined}>
                {typeof app === 'object' ? app.launchOrder + '.' : ''} {appName}
              </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/apps/${appName}`}
                className={`${listItemStyles.link} waves-effect`}>
            <i className={`${listItemStyles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (app: IValues): void => {
    this.props.onAddServiceApp(app as IAddServiceApp);
    this.setState({selectedApp: undefined});
  };

  private onRemove = (apps: string[]): void =>
    this.props.onRemoveServiceApps(apps);

  private onDeleteSuccess = (apps: string[]): void => {
    if (this.props.service?.serviceName) {
      const {serviceName} = this.props.service;
      this.props.removeServiceApps(serviceName, apps);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete app`, 10000, reason, true);

  private getSelectableAppsNames = () => {
    const {apps, serviceApps, unsavedApps} = this.props;
    const unsavedAppsNames = unsavedApps.map(app => app.name);
    return Object.keys(apps).filter(name => !serviceApps.includes(name) && !unsavedAppsNames.includes(name));
  };

  private appServicesLaunchOrder = (service: IAppService, index: number) =>
    <ListItem key={index}>
      <div className={`${appServiceStyles.appServiceItem}`}>
        <span>{service.launchOrder} - {service.service.serviceName}</span>
      </div>
    </ListItem>;

  private appServicesList = (): IAppService[] => {
    if (!this.state.selectedApp) {
      return [];
    }
    if (!this.props.apps[this.state.selectedApp]) {
      return [];
    }
    const services = this.props.apps[this.state.selectedApp].services;
    if (!services) {
      return [];
    }
    return Object.values(services)
                 .filter(service => service.service.serviceName !== this.props.service?.serviceName)
                 .sort((a, b) => a.launchOrder - b.launchOrder);
  };

  private serviceAppModal = () => {
    const OtherServicesList = List<IAppService>();
    const list = this.appServicesList();
    return (
      <>
        <Field key='launchOrder' id={'launchOrder'} label='launchOrder' type={'number'}/>
        <Collapsible id={'otherServicesList'}
                     title={'Other services\' launch order'}
                     onChange={this.updateModalScrollbar}>
          {list && <OtherServicesList list={list} show={this.appServicesLaunchOrder}/>}
        </Collapsible>
      </>
    )
  };

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
      launchOrder: undefined
    }
  );

  private onSelectApp = (selectedApp: string): void => {
    this.setState({selectedApp: selectedApp});
    this.props.loadAppServices(selectedApp);
  };

  private updateModalScrollbar = () =>
    this.scrollbar.current?.updateScroll();

}

function mapStateToProps(state: ReduxState, ownProps: ServiceAppListProps): StateToProps {
  const serviceName = ownProps.service?.serviceName;
  const service = serviceName && state.entities.services.data[serviceName];
  const serviceApps = service && service.apps;
  return {
    isLoading: state.entities.services.isLoadingApps,
    error: state.entities.services.loadAppsError,
    apps: state.entities.apps.data,
    serviceApps: serviceApps || [],
    isLoadingAppServices: state.entities.apps.isLoadingServices,
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadApps,
    loadServiceApps,
    loadAppServices,
    removeServiceApps,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServiceAppList);