/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import BaseComponent from "../../components/BaseComponent";
import React from "react";
import {RouteComponentProps} from "react-router";
import Form, {IFields, requiredAndNumberAndMin, requiredAndTrimmed} from "../../components/form/Form";
import ListLoadingSpinner from "../../components/list/ListLoadingSpinner";
import Error from "../../components/errors/Error";
import Field, {getTypeFromValue} from "../../components/form/Field";
import Tabs, {Tab} from "../../components/tabs/Tabs";
import MainLayout from "../../views/mainLayout/MainLayout";
import {ReduxState} from "../../reducers";
import {
  loadLoadBalancers,
  addLoadBalancer,
  loadRegions,
  loadServices
} from "../../actions";
import {connect} from "react-redux";
import {IService} from "../services/Service";
import {IRegion} from "../region/Region";
import {IReply} from "../../utils/api";
import {isNew} from "../../utils/router";
import {IContainer} from "../containers/Container";
import {normalize} from "normalizr";
import {Schemas} from "../../middleware/api";

export interface ILoadBalancer extends IContainer {
}

interface INewLoadBalancer {
  service: IService | undefined,
  regions: string[] | undefined
}

const buildNewLoadBalancer = (): INewLoadBalancer => ({
  service: undefined,
  regions: undefined
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  newLoadBalancer?: INewLoadBalancer;
  loadBalancer?: ILoadBalancer;
  formLoadBalancer?: Partial<ILoadBalancer> | INewLoadBalancer;
  services: { [key: string]: IService };
  regions: { [key: string]: IRegion };
}

interface DispatchToProps {
  loadLoadBalancers: (id: string) => void;
  addLoadBalancer: (loadBalancer: IContainer) => void;
  loadServices: () => any;
  loadRegions: () => any;
}

interface MatchParams {
  id: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  loadBalancer?: ILoadBalancer,
  formLoadBalancer?: ILoadBalancer,
}

class LoadBalancer extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
  };

  componentDidMount(): void {
    this.loadLoadBalancer();
    this.props.loadServices();
    this.props.loadRegions();
    this.mounted = true;
  }

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadLoadBalancer = () => {
    if (!isNew(this.props.location.search)) {
      const loadBalancerId = this.props.match.params.id;
      this.props.loadLoadBalancers(loadBalancerId);
    }
  };

  private getLoadBalancer = () =>
    this.state.loadBalancer || this.props.loadBalancer;

  private getFormLoadBalancer = () =>
    this.state.formLoadBalancer || this.props.formLoadBalancer;

  private onPostSuccess = (reply: IReply<ILoadBalancer[]>): void => {
    const loadBalancers = reply.data;
    loadBalancers.forEach(loadBalancer => {
        super.toast(`<span class="green-text">Load-balancer ${this.mounted ? `<b class="white-text">${loadBalancer.id}</b>` : `<a href=/load-balancers/${loadBalancer.id}><b>${loadBalancer.id}</b></a>`} launched</span>`);
        this.props.addLoadBalancer(loadBalancer);
    });
    if (this.mounted) {
      if (loadBalancers.length === 1) {
        const loadBalancer = loadBalancers[0];
        this.updateLoadBalancer(loadBalancer);
        this.props.history.replace(loadBalancer.id)
      }
      else {
        this.props.history.push('/load-balancers');
      }
    }
  };

  private onPostFailure = (reason: string): void =>
    super.toast(`Unable to launch load-balancer`, 10000, reason, true);

  private onDeleteSuccess = (loadBalancer: ILoadBalancer): void => {
    super.toast(`<span class="green-text">Load-balancer <b class="white-text">${loadBalancer.id}</b> successfully stopped</span>`);
    if (this.mounted) {
      this.props.history.push(`/load-balancers`)
    }
  };

  private onDeleteFailure = (reason: string, loadBalancer: ILoadBalancer): void =>
    super.toast(`Unable to stop ${this.mounted ? `<b>${loadBalancer.id}</b>` : `<a href=/load-balancers/${loadBalancer.id}><b>${loadBalancer.id}</b></a>`} load-balancer`, 10000, reason, true);

  private updateLoadBalancer = (loadBalancer: ILoadBalancer) => {
    //const previousLoadBalancer = this.getLoadBalancer();
    loadBalancer = Object.values(normalize(loadBalancer, Schemas.LOAD_BALANCER).entities.loadBalancers || {})[0];
    //TODO this.props.updateLoadBalancer(previousLoadBalancer, loadBalancer);
    const formLoadBalancer = { ...loadBalancer };
    removeFields(formLoadBalancer);
    this.setState({loadBalancer: loadBalancer, formLoadBalancer: formLoadBalancer});
  };

  private getFields = (loadBalancer: Partial<ILoadBalancer> | INewLoadBalancer): IFields =>
    Object.entries(loadBalancer).map(([key, value]) => {
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

  private getSelectableServices = () =>
    Object.entries(this.props.services)
          .filter(([_, service]) => service.serviceType === 'frontend')
          .map(([key, _]) => key);

  private loadBalancer = () => {
    const {isLoading, error, newLoadBalancer} = this.props;
    const loadBalancer = this.getLoadBalancer();
    const formLoadBalancer = this.getFormLoadBalancer();
    // @ts-ignore
    const loadBalancerKey: (keyof ILoadBalancer) = formLoadBalancer && Object.keys(formLoadBalancer)[0];
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formLoadBalancer && (
          <Form id={loadBalancerKey}
                fields={this.getFields(formLoadBalancer || {})}
                values={loadBalancer || newLoadBalancer || {}}
                isNew={isNew(this.props.location.search)}
                post={{
                  textButton: 'launch',
                  url: 'containers/loadBalancer',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                delete={{
                  textButton: 'Stop',
                  url: `containers/${loadBalancer?.id}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}>
            {Object.entries(formLoadBalancer).map((([key, value], index) =>
                key === 'service'
                  ? <Field key={index}
                           id={key}
                           label={key}
                           type={'dropdown'}
                           dropdown={{
                             defaultValue: "Select service",
                             values: this.getSelectableServices()}}/>
                  : key === 'regions'
                  ? <Field key={index}
                           id={key}
                           label={key}
                           type={'list'}
                           value={value}/>
                  : <Field key={index}
                           id={key}
                           label={key}/>
            ))}
          </Form>
        )}
      </>
    )
  };

  private tabs: Tab[] = [
    {
      title: 'Load balancer',
      id: 'loadBalancer',
      content: () => this.loadBalancer()
    },
  ];

  render() {
    return (
      <MainLayout>
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs}/>
        </div>
      </MainLayout>
    );
  }

}

function removeFields(container: Partial<ILoadBalancer>) {
  delete container["ports"];
  delete container["labels"];
  delete container["logs"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.loadBalancers.isLoadingLoadBalancers;
  const error = state.entities.loadBalancers.loadLoadBalancersError;
  const id = props.match.params.id;
  const newLoadBalancer = isNew(props.location.search) ? buildNewLoadBalancer() : undefined;
  const loadBalancer = !isNew(props.location.search) ? state.entities.loadBalancers.data[id] : undefined;
  const regions = state.entities.regions.data;
  let formLoadBalancer;
  if (newLoadBalancer) {
    formLoadBalancer = { ...newLoadBalancer, regions: Object.keys(regions) };
  }
  if (loadBalancer) {
    formLoadBalancer = { ...loadBalancer };
    removeFields(formLoadBalancer);
  }
  const services = state.entities.services.data;
  return  {
    isLoading,
    error,
    newLoadBalancer,
    loadBalancer,
    formLoadBalancer,
    services,
    regions
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadLoadBalancers,
  addLoadBalancer,
  loadServices,
  loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(LoadBalancer);
