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

import IDatabaseData from "../../../../components/IDatabaseData";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../../components/BaseComponent";
import Form, {
  IFields,
  requiredAndTrimmed,
  requiredAndTrimmedAndNotValidIpAddress
} from "../../../../components/form/Form";
import ListLoadingSpinner from "../../../../components/list/ListLoadingSpinner";
import {Error} from "../../../../components/errors/Error";
import Field from "../../../../components/form/Field";
import Tabs, {Tab} from "../../../../components/tabs/Tabs";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../../reducers";
import {
  addEdgeHost,
  addEdgeHostRules,
  addEdgeHostSimulatedMetrics,
  loadEdgeHosts,
  loadRegions,
  updateEdgeHost
} from "../../../../actions";
import {connect} from "react-redux";
import React from "react";
import EdgeHostRuleList from "./EdgeHostRuleList";
import {IReply, postData} from "../../../../utils/api";
import GenericHostRuleList from "../GenericHostRuleList";
import UnsavedChanged from "../../../../components/form/UnsavedChanges";
import {isNew} from "../../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../../middleware/api";
import EdgeHostSimulatedMetricList from "./EdgeHostSimulatedMetricList";
import GenericSimulatedHostMetricList from "../GenericSimulatedHostMetricList";
import {IRegion} from "../../region/Region";

export interface IEdgeHost extends IDatabaseData {
  username: string;
  publicDnsName: string;
  privateIpAddress: string;
  publicIpAddress: string;
  region: IRegion;
  country: string;
  city: string;
  hostRules?: string[];
  hostSimulatedMetrics?: string[];
}

interface INewEdgeHost extends IEdgeHost {
  password: string;
}

const buildNewEdgeHost = (): Partial<INewEdgeHost> => ({
  username: undefined,
  password: undefined,
  publicDnsName: undefined,
  privateIpAddress: undefined,
  publicIpAddress: undefined,
  region: undefined,
  country: undefined,
  city: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  edgeHost: Partial<IEdgeHost>;
  formEdgeHost?: Partial<IEdgeHost>;
  regions: { [key: string]: IRegion };
}

interface DispatchToProps {
  loadEdgeHosts: (hostname: string) => void;
  addEdgeHost: (edgeHost: IEdgeHost) => void;
  updateEdgeHost: (previousEdgeHost: IEdgeHost, currentEdgeHost: IEdgeHost) => void;
  loadRegions: () => void;
  addEdgeHostRules: (hostname: string, rules: string[]) => void;
  addEdgeHostSimulatedMetrics: (hostname: string, simulatedMetrics: string[]) => void;
}

interface MatchParams {
  hostname: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  edgeHost?: IEdgeHost,
  formEdgeHost?: IEdgeHost,
  unsavedRules: string[],
  unsavedSimulatedMetrics: string[],
}

class EdgeHost extends BaseComponent<Props, State> {

  state: State = {
    unsavedRules: [],
    unsavedSimulatedMetrics: [],
  };
  private mounted = false;

  public componentDidMount(): void {
    this.props.loadRegions();
    this.loadEdgeHost();
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

  private loadEdgeHost = () => {
    if (!isNew(this.props.location.search)) {
      const hostname = this.props.match.params.hostname;
      this.props.loadEdgeHosts(hostname);
    }
  };

  private getEdgeHost = () =>
    this.state.edgeHost || this.props.edgeHost;

  private getFormEdgeHost = () =>
    this.state.formEdgeHost || this.props.formEdgeHost;

  private isNew = () =>
    isNew(this.props.location.search);

  private onPostSuccess = (reply: IReply<IEdgeHost>): void => {
    const edgeHost = reply.data;
    const hostname = edgeHost.publicDnsName || edgeHost.publicIpAddress;
    super.toast(`<span class="green-text">Edge host ${this.mounted ? `<b class="white-text">${hostname}</b>` : `<a href=/hosts/edge/${hostname}><b>${hostname}</b></a>`} saved</span>`);
    this.props.addEdgeHost(edgeHost);
    this.saveEntities(edgeHost);
    if (this.mounted) {
      this.updateEdgeHost(edgeHost);
      this.props.history.replace(hostname);
    }
  };

  private onPostFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to save <b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b> edge host`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IEdgeHost>): void => {
    const edgeHost = reply.data;
    const hostname = edgeHost.publicDnsName || edgeHost.publicIpAddress;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${hostname}</b>` : `<a href=/hosts/edge/${hostname}><b>${hostname}</b></a>`} edge host have been saved</span>`);
    this.saveEntities(edgeHost);
    const previousEdgeHost = this.getEdgeHost();
    if (previousEdgeHost.id) {
      this.props.updateEdgeHost(previousEdgeHost as IEdgeHost, edgeHost);
    }
    if (this.mounted) {
      this.updateEdgeHost(edgeHost);
      this.props.history.replace(edgeHost.publicDnsName || edgeHost.publicIpAddress);
    }
  };

  private onPutFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b>` : `<a href=/hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}><b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b></a>`} edge host`, 10000, reason, true);

  private onDeleteSuccess = (edgeHost: IEdgeHost): void => {
    super.toast(`<span class="green-text">Edge host <b class="white-text">${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/hosts/edge`)
    }
  };

  private onDeleteFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b>` : `<a href=/hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}><b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b></a>`} edge host`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedRules.length
    || !!this.state.unsavedSimulatedMetrics.length;

  private saveEntities = (edgeHost: IEdgeHost) => {
    this.saveEdgeHostRules(edgeHost);
    this.saveEdgeHostSimulatedMetrics(edgeHost);
  };

  private addEdgeHostRule = (rule: string): void => {
    this.setState({
      unsavedRules: this.state.unsavedRules.concat(rule)
    });
  };

  private removeEdgeHostRules = (rules: string[]): void => {
    this.setState({
      unsavedRules: this.state.unsavedRules.filter(rule => !rules.includes(rule))
    });
  };

  private saveEdgeHostRules = (edgeHost: IEdgeHost): void => {
    const {unsavedRules} = this.state;
    if (unsavedRules.length) {
      postData(`hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}/rules`, unsavedRules,
        () => this.onSaveRulesSuccess(edgeHost),
        (reason) => this.onSaveRulesFailure(edgeHost, reason));
    }
  };

  private onSaveRulesSuccess = (edgeHost: IEdgeHost): void => {
    this.props.addEdgeHostRules(edgeHost.publicDnsName || edgeHost.publicIpAddress, this.state.unsavedRules);
    if (this.mounted) {
      this.setState({unsavedRules: []});
    }
  };

  private onSaveRulesFailure = (edgeHost: IEdgeHost, reason: string): void =>
    super.toast(`Unable to save rules of ${this.mounted ? `<b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b>` : `<a href=/hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}><b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b></a>`} edge host`, 10000, reason, true);

  private removeHostSimulatedMetrics = (simulatedMetrics: string[]): void => {
    this.setState({
      unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.filter(metric => !simulatedMetrics.includes(metric))
    });
  };

  private addHostSimulatedMetric = (simulatedMetric: string): void => {
    this.setState({
      unsavedSimulatedMetrics: this.state.unsavedSimulatedMetrics.concat(simulatedMetric)
    });
  };

  private saveEdgeHostSimulatedMetrics = (edgeHost: IEdgeHost): void => {
    const {unsavedSimulatedMetrics} = this.state;
    if (unsavedSimulatedMetrics.length) {
      postData(`hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}/simulated-metrics`, unsavedSimulatedMetrics,
        () => this.onSaveSimulatedMetricsSuccess(edgeHost),
        (reason) => this.onSaveSimulatedMetricsFailure(edgeHost, reason));
    }
  };

  private onSaveSimulatedMetricsSuccess = (edgeHost: IEdgeHost): void => {
    this.props.addEdgeHostSimulatedMetrics(edgeHost.publicDnsName || edgeHost.publicIpAddress, this.state.unsavedSimulatedMetrics);
    if (this.mounted) {
      this.setState({unsavedSimulatedMetrics: []});
    }
  };

  private onSaveSimulatedMetricsFailure = (edgeHost: IEdgeHost, reason: string): void =>
    super.toast(`Unable to save simulated metrics of ${this.mounted ? `<b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b>` : `<a href=/hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}><b>${edgeHost.publicDnsName || edgeHost.publicIpAddress}</b></a>`} edge host`, 10000, reason, true);

  private updateEdgeHost = (edgeHost: IEdgeHost) => {
    edgeHost = Object.values(normalize(edgeHost, Schemas.EDGE_HOST).entities.edgeHosts || {})[0];
    const formEdgeHost = {...edgeHost};
    removeFields(formEdgeHost);
    this.setState({edgeHost: edgeHost, formEdgeHost: formEdgeHost});
  };

  private getFields = (edgeHost: Partial<IEdgeHost>): IFields =>
    Object.entries(edgeHost).map(([key, _]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation:
            key.toLowerCase().includes('address')
              ? {rule: requiredAndTrimmedAndNotValidIpAddress}
              : {rule: requiredAndTrimmed}
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private getSelectableRegions = () =>
    Object.values(this.props.regions);

  private regionDropdownOption = (region: IRegion) =>
    region.name;

  private edgeHost = () => {
    const {isLoading, error} = this.props;
    const edgeHost = this.getEdgeHost();
    const formEdgeHost = this.getFormEdgeHost();
    // @ts-ignore
    const edgeHostKey: (keyof IEdgeHost) = formEdgeHost && Object.keys(formEdgeHost)[0];
    const isNewEdgeHost = this.isNew();
    return (
      <>
        {!isNewEdgeHost && isLoading && <ListLoadingSpinner/>}
        {!isNewEdgeHost && !isLoading && error && <Error message={error}/>}
        {(isNewEdgeHost || !isLoading) && (isNewEdgeHost || !error) && formEdgeHost && (
          <Form id={edgeHostKey}
                fields={this.getFields(formEdgeHost)}
                values={edgeHost}
                isNew={isNew(this.props.location.search)}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  url: 'hosts/edge',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `hosts/edge/${edgeHost.publicDnsName || edgeHost.publicIpAddress}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.keys(formEdgeHost).map((key, index) =>
              key === 'local'
                ? <Field<boolean> key={index}
                                  id={key}
                                  type="dropdown"
                                  label={key}
                                  dropdown={{
                                    defaultValue: "Is a local machine?",
                                    values: [true, false]
                                  }}/>
                : key === 'region'
                ? <Field<IRegion> key='region'
                                  id={'region'}
                                  label='region'
                                  type={'dropdown'}
                                  dropdown={{
                                    defaultValue: 'Select region',
                                    values: this.getSelectableRegions(),
                                    optionToString: this.regionDropdownOption
                                  }}/>
                : key === 'password'
                  ? <Field key={index}
                           id={key}
                           label={key}
                           hidden={true}/>
                  : <Field key={index}
                           id={key}
                           label={key}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private rules = (): JSX.Element =>
    <EdgeHostRuleList isLoadingEdgeHost={this.props.isLoading}
                      loadEdgeHostError={!this.isNew() ? this.props.error : undefined}
                      edgeHost={this.getEdgeHost()}
                      unsavedRules={this.state.unsavedRules}
                      onAddHostRule={this.addEdgeHostRule}
                      onRemoveHostRules={this.removeEdgeHostRules}/>;

  private genericRules = (): JSX.Element =>
    <GenericHostRuleList/>;

  private simulatedMetrics = (): JSX.Element =>
    <EdgeHostSimulatedMetricList isLoadingEdgeHost={this.props.isLoading}
                                 loadEdgeHostError={!this.isNew() ? this.props.error : undefined}
                                 edgeHost={this.getEdgeHost()}
                                 unsavedSimulatedMetrics={this.state.unsavedSimulatedMetrics}
                                 onAddSimulatedHostMetric={this.addHostSimulatedMetric}
                                 onRemoveSimulatedHostMetrics={this.removeHostSimulatedMetrics}/>;

  private genericSimulatedMetrics = (): JSX.Element =>
    <GenericSimulatedHostMetricList/>;

  private tabs = (): Tab[] => [
    {
      title: 'Edge host',
      id: 'edgeHost',
      content: () => this.edgeHost()
    },
    {
      title: 'Rules',
      id: 'edgeRules',
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
    },
  ];

}

function removeFields(edgeHost: Partial<IEdgeHost>) {
  delete edgeHost["id"];
  delete edgeHost["hostRules"];
  delete edgeHost["hostSimulatedMetrics"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.hosts.edge.isLoadingHosts;
  const error = state.entities.hosts.edge.loadHostsError;
  const hostname = props.match.params.hostname;
  const edgeHost = isNew(props.location.search) ? buildNewEdgeHost() : state.entities.hosts.edge.data[hostname];
  let formEdgeHost;
  if (edgeHost) {
    formEdgeHost = {...edgeHost};
    removeFields(formEdgeHost);
  }
  return {
    isLoading,
    error,
    edgeHost,
    formEdgeHost,
    regions: state.entities.regions.data,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadEdgeHosts,
  addEdgeHost,
  updateEdgeHost,
  addEdgeHostRules,
  addEdgeHostSimulatedMetrics,
  loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHost);