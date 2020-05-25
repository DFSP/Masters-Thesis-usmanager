/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import IDatabaseData from "../../../components/IDatabaseData";
import {RouteComponentProps} from "react-router";
import BaseComponent from "../../../components/BaseComponent";
import Form, {IFields, requiredAndTrimmed} from "../../../components/form/Form";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import Error from "../../../components/errors/Error";
import Field from "../../../components/form/Field";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
  addSimulatedHostMetric,
  addSimulatedHostMetricCloudHosts, addSimulatedHostMetricEdgeHosts,
  loadFields,
  loadSimulatedHostMetrics
} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../utils/api";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedHostMetricCloudHostList from "./SimulatedHostMetricCloudHostList";
import SimulatedHostMetricEdgeHostList from "./SimulatedHostMetricEdgeHostList";

export interface ISimulatedHostMetric extends IDatabaseData {
  name: string;
  field: IField;
  minimumValue: number;
  maximumValue: number;
  override: boolean;
  generic: boolean;
  cloudHosts?: string[];
  edgeHosts?: string[];
}

const buildNewSimulatedHostMetric = (): Partial<ISimulatedHostMetric> => ({
  name: undefined,
  field: undefined,
  minimumValue: undefined,
  maximumValue: undefined,
  override: undefined,
  generic: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  simulatedHostMetric: Partial<ISimulatedHostMetric>;
  formSimulatedHostMetric?: Partial<ISimulatedHostMetric>;
  fields: { [key:string]: IField };
}

interface DispatchToProps {
  loadSimulatedHostMetrics: (name: string) => void;
  addSimulatedHostMetric: (simulatedHostMetric: ISimulatedHostMetric) => void;
  //TODO updateSimulatedHostMetric: (previousSimulatedHostMetric: Partial<ISimulatedHostMetric>,
  // simulatedHostMetric: ISimulatedHostMetric) => void;
  loadFields: () => void;
  addSimulatedHostMetricCloudHosts: (name: string, cloudHosts: string[]) => void;
  addSimulatedHostMetricEdgeHosts: (name: string, edgeHosts: string[]) => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  simulatedHostMetric?: ISimulatedHostMetric,
  formSimulatedHostMetric?: ISimulatedHostMetric,
  unsavedCloudHosts: string[],
  unsavedEdgeHosts: string[],
  isGeneric: boolean,
}

class SimulatedHostMetric extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
    unsavedEdgeHosts: [],
    unsavedCloudHosts: [],
    isGeneric: this.props.simulatedHostMetric?.generic || false,
  };

  componentDidMount(): void {
    this.loadSimulatedHostMetric();
    this.props.loadFields();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadSimulatedHostMetric = () => {
    if (!isNew(this.props.location.search)) {
      const name = this.props.match.params.name;
      this.props.loadSimulatedHostMetrics(name);
    }
  };

  private getSimulatedHostMetric = () =>
    this.state.simulatedHostMetric || this.props.simulatedHostMetric;

  private getFormSimulatedHostMetric = () =>
    this.state.formSimulatedHostMetric || this.props.formSimulatedHostMetric;

  private onPostSuccess = (reply: IReply<ISimulatedHostMetric>): void => {
    const simulatedMetric = reply.data;
    super.toast(`<span class="green-text">Simulated host metric ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} saved</span>`);
    this.props.addSimulatedHostMetric(simulatedMetric);
    this.saveEntities(simulatedMetric);
    if (this.mounted) {
      this.updateSimulatedHostMetric(simulatedMetric);
      this.props.history.replace(simulatedMetric.name);
    }
  };

  private onPostFailure = (reason: string, simulatedHostMetric: ISimulatedHostMetric): void =>
    super.toast(`Unable to save <b>${simulatedHostMetric.name}</b> simulated host metric`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<ISimulatedHostMetric>): void => {
    const simulatedMetric = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated host metric have been saved</span>`);
    this.saveEntities(simulatedMetric);
    if (this.mounted) {
      this.updateSimulatedHostMetric(simulatedMetric);
      this.props.history.replace(simulatedMetric.name);
    }
  };

  private onPutFailure = (reason: string, simulatedMetric: ISimulatedHostMetric): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated host metric`, 10000, reason, true);

  private onDeleteSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
    super.toast(`<span class="green-text">Simulated host metric <b class="white-text">${simulatedMetric.name}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/simulated-metrics/hosts`);
    }
  };

  private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedHostMetric): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated host metric`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedCloudHosts.length
    || !!this.state.unsavedEdgeHosts.length;

  private saveEntities = (simulatedMetric: ISimulatedHostMetric) => {
    this.saveSimulatedHostMetricCloudHosts(simulatedMetric);
    this.saveSimulatedHostMetricEdgeHosts(simulatedMetric);
  };

  private addSimulatedHostMetricCloudHost = (cloudHost: string): void => {
    this.setState({
      unsavedCloudHosts: this.state.unsavedCloudHosts.concat(cloudHost)
    });
  };

  private removeSimulatedHostMetricCloudHosts = (cloudHosts: string[]): void => {
    this.setState({
      unsavedCloudHosts: this.state.unsavedCloudHosts.filter(cloudHost => !cloudHosts.includes(cloudHost))
    });
  };

  private saveSimulatedHostMetricCloudHosts = (simulatedMetric: ISimulatedHostMetric): void => {
    const {unsavedCloudHosts} = this.state;
    if (unsavedCloudHosts.length) {
      postData(`simulated-metrics/hosts/${simulatedMetric.name}/cloud-hosts`, unsavedCloudHosts,
        () => this.onSaveCloudHostsSuccess(simulatedMetric),
        (reason) => this.onSaveCloudHostsFailure(simulatedMetric, reason));
    }
  };

  private onSaveCloudHostsSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
    this.props.addSimulatedHostMetricCloudHosts(simulatedMetric.name, this.state.unsavedCloudHosts);
    if (this.mounted) {
      this.setState({ unsavedCloudHosts: [] });
    }
  };

  private onSaveCloudHostsFailure = (simulatedMetric: ISimulatedHostMetric, reason: string): void =>
    super.toast(`Unable to save cloud hosts of ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated host metric`, 10000, reason, true);

  private addSimulatedHostMetricEdgeHost = (edgeHost: string): void => {
    this.setState({
      unsavedEdgeHosts: this.state.unsavedEdgeHosts.concat(edgeHost)
    });
  };

  private removeSimulatedHostMetricEdgeHosts = (edgeHosts: string[]): void => {
    this.setState({
      unsavedEdgeHosts: this.state.unsavedEdgeHosts.filter(edgeHost => !edgeHosts.includes(edgeHost))
    });
  };

  private saveSimulatedHostMetricEdgeHosts = (simulatedMetric: ISimulatedHostMetric): void => {
    const {unsavedEdgeHosts} = this.state;
    if (unsavedEdgeHosts.length) {
      postData(`simulated-metrics/hosts/${simulatedMetric.name}/edge-hosts`, unsavedEdgeHosts,
        () => this.onSaveEdgeHostsSuccess(simulatedMetric),
        (reason) => this.onSaveEdgeHostsFailure(simulatedMetric, reason));
    }
  };

  private onSaveEdgeHostsSuccess = (simulatedMetric: ISimulatedHostMetric): void => {
    this.props.addSimulatedHostMetricEdgeHosts(simulatedMetric.name, this.state.unsavedEdgeHosts);
    if (this.mounted) {
      this.setState({ unsavedEdgeHosts: [] });
    }
  };

  private onSaveEdgeHostsFailure = (simulatedMetric: ISimulatedHostMetric, reason: string): void =>
    super.toast(`Unable to save edge hosts of ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/hosts/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated host metric`, 10000, reason, true);

  private updateSimulatedHostMetric = (simulatedHostMetric: ISimulatedHostMetric) => {
    //const previousSimulatedMetric = this.getSimulatedHostMetric();
    simulatedHostMetric = Object.values(normalize(simulatedHostMetric, Schemas.SIMULATED_HOST_METRIC).entities.simulatedHostMetrics || {})[0];
    //TODO this.props.updateSimulatedHostMetric(previousSimulatedMetric, simulatedMetric);
    const formSimulatedHostMetric = { ...simulatedHostMetric };
    removeFields(formSimulatedHostMetric);
    this.setState({simulatedHostMetric: simulatedHostMetric, formSimulatedHostMetric: formSimulatedHostMetric});
  };

  private getFields = (simulatedHostMetric: Partial<ISimulatedHostMetric>): IFields =>
    Object.entries(simulatedHostMetric).map(([key, _]) => {
      return {
        [key]: {
          id: key,
          label: key,
          validation: { rule: requiredAndTrimmed }
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private fieldOption = (field: IField): string =>
    field.name;

  private isGenericSelected = (value: string) =>
    this.setState({isGeneric: value.toLowerCase() === 'true'});

  private simulatedHostMetric = () => {
    const {isLoading, error} = this.props;
    const simulatedHostMetric = this.getSimulatedHostMetric();
    const formSimulatedHostMetric = this.getFormSimulatedHostMetric();
    // @ts-ignore
    const simulatedHostMetricKey: (keyof ISimulatedHostMetric) = formSimulatedHostMetric && Object.keys(formSimulatedHostMetric)[0];
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formSimulatedHostMetric && (
          <Form id={simulatedHostMetricKey}
                fields={this.getFields(formSimulatedHostMetric)}
                values={simulatedHostMetric}
                isNew={isNew(this.props.location.search)}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  url: 'simulated-metrics/hosts',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `simulated-metrics/hosts/${simulatedHostMetric.name}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `simulated-metrics/hosts/${simulatedHostMetric.name}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.keys(formSimulatedHostMetric).map((key, index) =>
              key === 'field'
                ? <Field<IField> key='fields'
                                 id='field'
                                 label='field'
                                 type='dropdown'
                                 dropdown={{
                                   defaultValue: "Select field",
                                   values: Object.values(this.props.fields),
                                   optionToString: this.fieldOption}}/>
                : key === 'override'
                ? <Field key={index}
                         id={key}
                         label={key}
                         type="dropdown"
                         dropdown={{
                           defaultValue: "Override true metrics?",
                           values: ['True', 'False']}}/>
                : key === 'generic'
                  ? <Field key={index}
                           id={key}
                           label={key}
                           type="dropdown"
                           dropdown={{
                             selectCallback: this.isGenericSelected,
                             defaultValue: "Apply to all hosts?",
                             values: ['True', 'False']}}/>
                  : <Field key={index}
                           id={key}
                           label={key}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private cloudHosts = (): JSX.Element =>
    <SimulatedHostMetricCloudHostList isLoadingSimulatedHostMetric={this.props.isLoading}
                                      loadSimulatedHostMetricError={this.props.error}
                                      simulatedHostMetric={this.getSimulatedHostMetric()}
                                      unsavedCloudHosts={this.state.unsavedCloudHosts}
                                      onAddCloudHost={this.addSimulatedHostMetricCloudHost}
                                      onRemoveCloudHosts={this.removeSimulatedHostMetricCloudHosts}/>;

  private edgeHosts = (): JSX.Element =>
    <SimulatedHostMetricEdgeHostList isLoadingSimulatedHostMetric={this.props.isLoading}
                                     loadSimulatedHostMetricError={this.props.error}
                                     simulatedHostMetric={this.getSimulatedHostMetric()}
                                     unsavedEdgeHosts={this.state.unsavedEdgeHosts}
                                     onAddEdgeHost={this.addSimulatedHostMetricEdgeHost}
                                     onRemoveEdgeHosts={this.removeSimulatedHostMetricEdgeHosts}/>;

  private tabs = () => [
    {
      title: 'Simulated metric',
      id: 'simulatedHostMetric',
      content: () => this.simulatedHostMetric(),
    },
    {
      title: 'Cloud hosts',
      id: 'cloudHosts',
      content: () => this.cloudHosts(),
      disabled: this.state.isGeneric,
    },
    {
      title: 'Edge hosts',
      id: 'edgeHosts',
      content: () => this.edgeHosts(),
      disabled: this.state.isGeneric,
    },
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

function removeFields(simulatedHostMetric: Partial<ISimulatedHostMetric>) {
  delete simulatedHostMetric["id"];
  delete simulatedHostMetric["cloudHosts"];
  delete simulatedHostMetric["edgeHosts"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.simulatedMetrics.hosts.isLoadingSimulatedHostMetrics;
  const error = state.entities.simulatedMetrics.hosts.loadSimulatedHostMetricsError;
  const name = props.match.params.name;
  const simulatedHostMetric = isNew(props.location.search) ? buildNewSimulatedHostMetric() : state.entities.simulatedMetrics.hosts.data[name];
  let formSimulatedHostMetric;
  if (simulatedHostMetric) {
    formSimulatedHostMetric = { ...simulatedHostMetric };
    removeFields(formSimulatedHostMetric);
  }
  const fields = state.entities.fields.data;
  return  {
    isLoading,
    error,
    simulatedHostMetric,
    formSimulatedHostMetric,
    fields
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadSimulatedHostMetrics,
  addSimulatedHostMetric,
  //TODO updateSimulatedHostMetric,
  loadFields,
  addSimulatedHostMetricCloudHosts,
  addSimulatedHostMetricEdgeHosts
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedHostMetric);