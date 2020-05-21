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
import {addEdgeHost, addEdgeHostRules, loadEdgeHosts} from "../../../actions";
import {connect} from "react-redux";
import React from "react";
import EdgeHostRuleList from "./EdgeHostRuleList";
import {IReply, postData} from "../../../utils/api";
import GenericHostRuleList from "../GenericHostRuleList";
import UnsavedChanged from "../../../components/form/UnsavedChanges";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";
import {IApp} from "../../apps/App";

export interface IEdgeHost extends IDatabaseData {
  hostname: string;
  sshUsername: string;
  sshPassword: string;
  region: string;
  country: string;
  city: string;
  local: boolean;
  hostRules?: string[];
}

const buildNewEdgeHost = (): Partial<IEdgeHost> => ({
  hostname: '',
  sshUsername: '',
  sshPassword: '',
  region: '',
  country: '',
  city: '',
  local: undefined,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  edgeHost: Partial<IEdgeHost>;
  formEdgeHost?: Partial<IEdgeHost>,
}

interface DispatchToProps {
  loadEdgeHosts: (hostname: string) => void;
  addEdgeHost: (edgeHost: IEdgeHost) => void;
  //TODO updateEdgeHost: (previousEdgeHost: Partial<IEdgeHost>, edgeHost: IEdgeHost) => void;
  addEdgeHostRules: (hostname: string, rules: string[]) => void;
}

interface MatchParams {
  hostname: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  edgeHost?: IEdgeHost,
  formEdgeHost?: IEdgeHost,
  unsavedRules: string[],
  hostname?: string,
}

class EdgeHost extends BaseComponent<Props, State> {

  private mounted = false;

  state: State = {
    unsavedRules: [],
  };

  componentDidMount(): void {
    this.loadEdgeHost();
  };

  componentWillUnmount(): void {
    this.mounted = false;
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

  private onPostSuccess = (reply: IReply<IEdgeHost>): void => {
    const edgeHost = reply.data;
    super.toast(`<span class="green-text">Edge host <b>${edgeHost.hostname}</b> is now saved</span>`);
    this.props.addEdgeHost(edgeHost);
    this.saveEntities(edgeHost);
    if (this.mounted) {
      this.updateEdgeHost(edgeHost);
      this.props.history.replace(edgeHost.hostname);
    }
  };

  private onPostFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to save ${edgeHost.hostname}`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IEdgeHost>): void => {
    const edgeHost = reply.data;
    super.toast(`<span class="green-text">Changes to host ${edgeHost.hostname} have been saved</span>`);
    this.saveEntities(edgeHost);
    if (this.mounted) {
      this.updateEdgeHost(edgeHost);
      this.props.history.replace(edgeHost.hostname);
    }
  };

  private onPutFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to update ${edgeHost.hostname}`, 10000, reason, true);

  private onDeleteSuccess = (edgeHost: IEdgeHost): void => {
    super.toast(`<span class="green-text">Edge host ${edgeHost.hostname} successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/hosts`)
    }
  };

  private onDeleteFailure = (reason: string, edgeHost: IEdgeHost): void =>
    super.toast(`Unable to remove edge host ${edgeHost.hostname}`, 10000, reason, true);

  private shouldShowSaveButton = () =>
    !!this.state.unsavedRules.length;

  private saveEntities = (edgeHost: IEdgeHost) => {
    this.saveEdgeHostRules(edgeHost);
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
      postData(`hosts/edge/${edgeHost.hostname}/rules`, unsavedRules,
        () => this.onSaveRulesSuccess(edgeHost),
        (reason) => this.onSaveRulesFailure(edgeHost, reason));
    }
  };

  private onSaveRulesSuccess = (edgeHost: IEdgeHost): void => {
    this.props.addEdgeHostRules(edgeHost.hostname, this.state.unsavedRules);
    if (this.mounted) {
      this.setState({ unsavedRules: [] });
    }
  };

  private onSaveRulesFailure = (edgeHost: IEdgeHost, reason: string): void =>
    super.toast(`Unable to save rules of host ${edgeHost.hostname}`, 10000, reason, true);

  private updateEdgeHost = (edgeHost: IEdgeHost) => {
    //const previousEdgeHost = this.getEdgeHost();
    edgeHost = Object.values(normalize(edgeHost, Schemas.EDGE_HOST).entities.edgeHosts || {})[0];
    //TODO this.props.updateEdgeHost(previousEdgeHost, edgeHost);
    const formEdgeHost = { ...edgeHost };
    removeFields(formEdgeHost);
    this.setState({edgeHost: edgeHost, formEdgeHost: formEdgeHost});
  };

  private getFields = (edgeHost: Partial<IEdgeHost>): IFields =>
    Object.entries(edgeHost).map(([key, _]) => {
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

  private edgeHost = () => {
    const {isLoading, error} = this.props;
    const edgeHost = this.getEdgeHost();
    const formEdgeHost = this.getFormEdgeHost();
    // @ts-ignore
    const edgeHostKey: (keyof IEdgeHost) = formEdgeHost && Object.keys(formEdgeHost)[0];
    return (
      <>
        {isLoading && <ListLoadingSpinner/>}
        {!isLoading && error && <Error message={error}/>}
        {!isLoading && !error && formEdgeHost && (
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
                  url: `hosts/edge/${edgeHost.hostname}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `hosts/edge/${edgeHost.hostname}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}
                saveEntities={this.saveEntities}>
            {Object.keys(formEdgeHost).map((key, index) =>
              key === 'local'
                ? <Field key={index}
                         id={key}
                         type="dropdown"
                         label={key}
                         dropdown={{
                           defaultValue: "Is a local machine?",
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

  private rules = (): JSX.Element =>
    <EdgeHostRuleList isLoadingEdgeHost={this.props.isLoading}
                      loadEdgeHostError={this.props.error}
                      edgeHost={this.getEdgeHost()}
                      unsavedRules={this.state.unsavedRules}
                      onAddHostRule={this.addEdgeHostRule}
                      onRemoveHostRules={this.removeEdgeHostRules}/>;

  private genericRules = (): JSX.Element =>
    <GenericHostRuleList/>;

  private tabs: Tab[] = [
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
  ];

  render() {
    return (
      <MainLayout>
        {this.shouldShowSaveButton() && !isNew(this.props.location.search) && <UnsavedChanged/>}
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs}/>
        </div>
      </MainLayout>
    );
  }

}

function removeFields(edgeHost: Partial<IEdgeHost>) {
  delete edgeHost["id"];
  delete edgeHost["hostRules"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.hosts.edge.isLoadingHosts;
  const error = state.entities.hosts.edge.loadHostsError;
  const hostname = props.match.params.hostname;
  const edgeHost = isNew(props.location.search) ? buildNewEdgeHost() : state.entities.hosts.edge.data[hostname];
  let formEdgeHost;
  if (edgeHost) {
    formEdgeHost = { ...edgeHost };
    removeFields(formEdgeHost);
  }
  return  {
    isLoading,
    error,
    edgeHost,
    formEdgeHost,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadEdgeHosts,
  addEdgeHost,
  //TODO updateEdgeHost,
  addEdgeHostRules
};

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHost);