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

import React from "react";
import {ReduxState} from "../../../reducers";
import {IService} from "./Service";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {
  addServiceDependencies,
  loadServiceDependencies,
  loadServices,
  removeServiceDependencies
} from "../../../actions";
import BaseComponent from "../../../components/BaseComponent";
import {Link} from "react-router-dom";
import ControlledList from "../../../components/list/ControlledList";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";

export interface IServiceDependency extends IService {
}

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  services: { [key: string]: IService },
  dependencies: string[];
}

interface DispatchToProps {
  loadServices: () => void;
  loadServiceDependencies: (serviceName: string) => void;
  removeServiceDependencies: (serviceName: string, dependencies: string[]) => void;
}

interface ServiceDependencyProps {
  isLoadingService: boolean;
  loadServiceError?: string | null;
  service: IService | Partial<IService> | null;
  unsavedDependencies: string[];
  onAddServiceDependency: (dependency: string) => void;
  onRemoveServiceDependencies: (dependencies: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceDependencyProps;

interface State {
  entitySaved: boolean;
}

class ServiceDependencyList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {entitySaved: !this.isNew()};
  }

  public componentDidMount(): void {
    this.props.loadServices();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (!prevProps.service?.serviceName && this.props.service?.serviceName) {
      this.setState({entitySaved: true});
    }
  }

  public render() {
    const isNew = this.isNew();
    return <ControlledList isLoading={!isNew ? this.props.isLoadingService || this.props.isLoading : undefined}
                           error={!isNew ? this.props.loadServiceError || this.props.error : undefined}
                           emptyMessage={`Dependencies list is empty`}
                           data={this.props.dependencies}
                           dropdown={{
                             id: 'dependencies',
                             title: 'Add dependency',
                             empty: 'No more dependencies to add',
                             data: this.getSelectableServicesNames()
                           }}
                           show={this.dependency}
                           onAdd={this.onAdd}
                           onRemove={this.onRemove}
                           onDelete={{
                             url: `services/${this.props.service?.serviceName}/dependencies`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                           }}
                           entitySaved={this.state.entitySaved}/>;

  }

  private loadEntities = () => {
    if (this.props.service?.serviceName) {
      const {serviceName} = this.props.service;
      this.props.loadServiceDependencies(serviceName);
    }
  };

  private isNew = () =>
    this.props.service?.serviceName === undefined;

  private dependency = (index: number, dependency: string, separate: boolean, checked: boolean,
                        handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unsavedDependencies.includes(dependency);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${styles.linkedItemContent}`}>
          <label>
            <input id={dependency}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                 {dependency}
              </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/services/${dependency}`}
                className={`${styles.link} waves-effect`}>
            <i className={`${styles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (dependency: string): void =>
    this.props.onAddServiceDependency(dependency);

  private onRemove = (dependencies: string[]) =>
    this.props.onRemoveServiceDependencies(dependencies);

  private onDeleteSuccess = (dependencies: string[]): void => {
    if (this.props.service?.serviceName) {
      const {serviceName} = this.props.service;
      this.props.removeServiceDependencies(serviceName, dependencies);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete dependency`, 10000, reason, true);

  private getSelectableServicesNames = () => {
    const {services, service, dependencies, unsavedDependencies} = this.props;
    return Object.keys(services)
                 .filter(name => (!service || name !== service.serviceName) && !dependencies.includes(name) && !unsavedDependencies.includes(name));
  };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceDependencyProps): StateToProps {
  const serviceName = ownProps.service?.serviceName;
  const service = serviceName && state.entities.services.data[serviceName];
  const dependencies = service && service.dependencies;
  return {
    isLoading: state.entities.services.isLoadingDependencies,
    error: state.entities.services.loadDependenciesError,
    services: state.entities.services.data,
    dependencies: dependencies || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadServices,
    loadServiceDependencies,
    addServiceDependencies,
    removeServiceDependencies,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServiceDependencyList);