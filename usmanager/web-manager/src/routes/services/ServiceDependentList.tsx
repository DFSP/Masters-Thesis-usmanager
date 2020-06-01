/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import {IService} from "./Service";
import React from "react";
import ListItem from "../../components/list/ListItem";
import styles from "../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import BaseComponent from "../../components/BaseComponent";
import {ReduxState} from "../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import List from "../../components/list/List";
import {loadServiceDependents} from "../../actions";

export interface IDependent extends IService {
}

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  dependentNames: string[];
}

interface DispatchToProps {
  loadServiceDependents: (serviceName: string) => void;
}

interface ServiceDependentListProps {
  isLoadingService: boolean;
  loadServiceError?: string | null;
  service: IService | Partial<IService> | null;
}

type Props = StateToProps & DispatchToProps & ServiceDependentListProps;

class ServiceDependentList extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
    if (prevProps.service?.serviceName !== this.props.service?.serviceName) {
      this.loadEntities();
    }
  }

  private loadEntities = () => {
    if (this.props.service?.serviceName) {
      const {serviceName} = this.props.service;
      this.props.loadServiceDependents(serviceName);
    }
  };

  private dependent = (dependent: string, index: number): JSX.Element =>
    <ListItem key={index} separate={index !== this.props.dependentNames.length - 1}>
      <div className={`${styles.linkedItemContent}`}>
        <span>{dependent}</span>
      </div>
      <Link to={`/services/${dependent}`}
            className={`${styles.link} waves-effect`}>
        <i className={`${styles.linkIcon} material-icons right`}>link</i>
      </Link>
    </ListItem>;

  public render() {
    const DependentsList = List<string>();
    return (
      <DependentsList isLoading={this.props.isLoadingService || this.props.isLoading}
                     error={this.props.loadServiceError || this.props.error}
                     emptyMessage={`Dependents list is empty`}
                     list={this.props.dependentNames}
                     show={this.dependent}/>
    );
  }

}

function mapStateToProps(state: ReduxState, ownProps: ServiceDependentListProps): StateToProps {
  const serviceName = ownProps.service?.serviceName;
  const service = serviceName && state.entities.services.data[serviceName];
  const serviceDependents = service && service.dependents;
  return {
    isLoading: state.entities.services.isLoadingDependents,
    error: state.entities.services.loadDependentsError,
    dependentNames: serviceDependents || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadServiceDependents,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ServiceDependentList);