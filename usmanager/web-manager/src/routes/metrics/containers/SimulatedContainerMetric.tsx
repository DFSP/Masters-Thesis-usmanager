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
import {Error} from "../../../components/errors/Error";
import Field from "../../../components/form/Field";
import Tabs from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {
  addSimulatedContainerMetric,
  addSimulatedContainerMetricContainers,
  loadFields,
  loadSimulatedContainerMetrics
} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import {IReply, postData} from "../../../utils/api";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IField} from "../../rules/Rule";
import SimulatedContainerMetricContainerList from "./SimulatedContainerMetricContainerList";

export interface ISimulatedContainerMetric extends IDatabaseData {
  name: string;
  field: IField;
  minimumValue: number;
  maximumValue: number;
  override: boolean;
  generic: boolean;
  containers?: string[];
}

const buildNewSimulatedContainerMetric = (): Partial<ISimulatedContainerMetric> => ({
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
  simulatedContainerMetric: Partial<ISimulatedContainerMetric>;
  formSimulatedContainerMetric?: Partial<ISimulatedContainerMetric>;
  fields: { [key:string]: IField };
}

interface DispatchToProps {
  loadSimulatedContainerMetrics: (name: string) => void;
  addSimulatedContainerMetric: (simulatedContainerMetric: ISimulatedContainerMetric) => void;
  //TODO updateSimulatedContainerMetric: (previousSimulatedContainerMetric: Partial<ISimulatedContainerMetric>,
  // simulatedContainerMetric: ISimulatedContainerMetric) => void;
  loadFields: () => void;
  addSimulatedContainerMetricContainers: (name: string, containers: string[]) => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  simulatedContainerMetric?: ISimulatedContainerMetric,
  formSimulatedContainerMetric?: ISimulatedContainerMetric,
  unsavedContainers: string[],
  isGeneric: boolean,
}

class SimulatedContainerMetric extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
    unsavedContainers: [],
    isGeneric: this.props.simulatedContainerMetric?.generic || false,
  };

  public componentDidMount(): void {
    this.loadSimulatedContainerMetric();
    this.props.loadFields();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  private loadSimulatedContainerMetric = () => {
    if (!isNew(this.props.location.search)) {
      const name = this.props.match.params.name;
      this.props.loadSimulatedContainerMetrics(name);
    }
  };

  private getSimulatedContainerMetric = () =>
    this.state.simulatedContainerMetric || this.props.simulatedContainerMetric;

  private getFormSimulatedContainerMetric = () =>
    this.state.formSimulatedContainerMetric || this.props.formSimulatedContainerMetric;

  private onPostSuccess = (reply: IReply<ISimulatedContainerMetric>): void => {
    const simulatedMetric = reply.data;
    super.toast(`<span class="green-text">Simulated container metric ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/Containers/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} saved</span>`);
    this.props.addSimulatedContainerMetric(simulatedMetric);
    this.saveEntities(simulatedMetric);
    if (this.mounted) {
      this.updateSimulatedContainerMetric(simulatedMetric);
      this.props.history.replace(simulatedMetric.name);
    }
  };

  private onPostFailure = (reason: string, simulatedContainerMetric: ISimulatedContainerMetric): void =>
    super.toast(`Unable to save <b>${simulatedContainerMetric.name}</b> simulated container metric`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<ISimulatedContainerMetric>): void => {
    const simulatedMetric = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/Containers/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated container metric have been saved</span>`);
    this.saveEntities(simulatedMetric);
    if (this.mounted) {
      this.updateSimulatedContainerMetric(simulatedMetric);
      this.props.history.replace(simulatedMetric.name);
    }
  };

  private onPutFailure = (reason: string, simulatedMetric: ISimulatedContainerMetric): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/Containers/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated container metric`, 10000, reason, true);

  private onDeleteSuccess = (simulatedMetric: ISimulatedContainerMetric): void => {
    super.toast(`<span class="green-text">Simulated container metric <b class="white-text">${simulatedMetric.name}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/simulated-metrics/Containers`);
    }
  };

  private onDeleteFailure = (reason: string, simulatedMetric: ISimulatedContainerMetric): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/Containers/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated container metric`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedContainers.length;

  private saveEntities = (simulatedMetric: ISimulatedContainerMetric) => {
    this.saveSimulatedContainerMetricContainers(simulatedMetric);
  };

  private addSimulatedContainerMetricContainer = (container: string): void => {
    this.setState({
      unsavedContainers: this.state.unsavedContainers.concat(container)
    });
  };

  private removeSimulatedContainerMetricContainers = (containers: string[]): void => {
    this.setState({
      unsavedContainers: this.state.unsavedContainers.filter(container => !containers.includes(container))
    });
  };

  private saveSimulatedContainerMetricContainers = (simulatedMetric: ISimulatedContainerMetric): void => {
    const {unsavedContainers} = this.state;
    if (unsavedContainers.length) {
      postData(`simulated-metrics/containers/${simulatedMetric.name}/containers`, unsavedContainers,
        () => this.onSaveContainersSuccess(simulatedMetric),
        (reason) => this.onSaveContainersFailure(simulatedMetric, reason));
    }
  };

  private onSaveContainersSuccess = (simulatedMetric: ISimulatedContainerMetric): void => {
    this.props.addSimulatedContainerMetricContainers(simulatedMetric.name, this.state.unsavedContainers);
    if (this.mounted) {
      this.setState({ unsavedContainers: [] });
    }
  };

  private onSaveContainersFailure = (simulatedMetric: ISimulatedContainerMetric, reason: string): void =>
    super.toast(`Unable to save containers of ${this.mounted ? `<b>${simulatedMetric.name}</b>` : `<a href=/simulated-metrics/containers/${simulatedMetric.name}><b>${simulatedMetric.name}</b></a>`} simulated container metric`, 10000, reason, true);

  private updateSimulatedContainerMetric = (simulatedContainerMetric: ISimulatedContainerMetric) => {
    //const previousSimulatedMetric = this.getSimulatedContainerMetric();
    simulatedContainerMetric = Object.values(normalize(simulatedContainerMetric, Schemas.SIMULATED_SERVICE_METRIC).entities.simulatedContainerMetrics || {})[0];
    //TODO this.props.updateSimulatedContainerMetric(previousSimulatedMetric, simulatedMetric);
    const formSimulatedContainerMetric = { ...simulatedContainerMetric };
    removeFields(formSimulatedContainerMetric);
    this.setState({simulatedContainerMetric: simulatedContainerMetric, formSimulatedContainerMetric: formSimulatedContainerMetric});
  };

  private getFields = (simulatedContainerMetric: Partial<ISimulatedContainerMetric>): IFields =>
    Object.entries(simulatedContainerMetric).map(([key, _]) => {
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

  private simulatedContainerMetric = () => {
    const {isLoading, error} = this.props;
    const simulatedContainerMetric = this.getSimulatedContainerMetric();
    const formSimulatedContainerMetric = this.getFormSimulatedContainerMetric();
    // @ts-ignore
    const simulatedContainerMetricKey: (keyof ISimulatedContainerMetric) = formSimulatedContainerMetric && Object.keys(formSimulatedContainerMetric)[0];
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formSimulatedContainerMetric && (
          <Form id={simulatedContainerMetricKey}
                fields={this.getFields(formSimulatedContainerMetric)}
                values={simulatedContainerMetric}
                isNew={isNew(this.props.location.search)}
                showSaveButton={this.shouldShowSaveButton()}
                post={{
                  url: 'simulated-metrics/containers',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `simulated-metrics/containers/${simulatedContainerMetric.name}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `simulated-metrics/containers/${simulatedContainerMetric.name}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.keys(formSimulatedContainerMetric).map((key, index) =>
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
                             defaultValue: "Apply to all containers?",
                             values: ['True', 'False']}}/>
                  : key === 'minimumValue' || key === 'maximumValue'
                    ? <Field key={index}
                             id={key}
                             label={key}
                             type={'number'}/>
                    : <Field key={index}
                             id={key}
                             label={key}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private containers = (): JSX.Element =>
    <SimulatedContainerMetricContainerList isLoadingSimulatedContainerMetric={this.props.isLoading}
                                           loadSimulatedContainerMetricError={this.props.error}
                                           simulatedContainerMetric={this.getSimulatedContainerMetric()}
                                           unsavedContainers={this.state.unsavedContainers}
                                           onAddContainer={this.addSimulatedContainerMetricContainer}
                                           onRemoveContainers={this.removeSimulatedContainerMetricContainers}/>;

  private tabs = () => [
    {
      title: 'Simulated metric',
      id: 'simulatedContainerMetric',
      content: () => this.simulatedContainerMetric(),
    },
    {
      title: 'Containers',
      id: 'containers',
      content: () => this.containers(),
      disabled: this.state.isGeneric,
    },
  ];

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

function removeFields(simulatedContainerMetric: Partial<ISimulatedContainerMetric>) {
  delete simulatedContainerMetric["id"];
  delete simulatedContainerMetric["containers"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.simulatedMetrics.containers.isLoadingSimulatedContainerMetrics;
  const error = state.entities.simulatedMetrics.containers.loadSimulatedContainerMetricsError;
  const name = props.match.params.name;
  const simulatedContainerMetric = isNew(props.location.search) ? buildNewSimulatedContainerMetric() : state.entities.simulatedMetrics.containers.data[name];
  let formSimulatedContainerMetric;
  if (simulatedContainerMetric) {
    formSimulatedContainerMetric = { ...simulatedContainerMetric };
    removeFields(formSimulatedContainerMetric);
  }
  const fields = state.entities.fields.data;
  return  {
    isLoading,
    error,
    simulatedContainerMetric,
    formSimulatedContainerMetric,
    fields
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadSimulatedContainerMetrics,
  addSimulatedContainerMetric,
  //TODO updateSimulatedContainerMetric,
  loadFields,
  addSimulatedContainerMetricContainers,
};

export default connect(mapStateToProps, mapDispatchToProps)(SimulatedContainerMetric);