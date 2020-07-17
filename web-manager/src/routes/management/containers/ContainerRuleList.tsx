/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import React from "react";
import {IContainer} from "./Container";
import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {
  loadRulesContainer,
  loadContainerRules,
  removeContainerRules
} from "../../../actions";
import {connect} from "react-redux";
import {IRuleContainer} from "../rules/containers/RuleContainer";
import {Link} from "react-router-dom";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  rules: { [key: string]: IRuleContainer },
  rulesName: string[];
}

interface DispatchToProps {
  loadRulesContainer: (name?: string) => any;
  loadContainerRules: (containerId: string) => void;
  removeContainerRules: (containerId: string, rules: string[]) => void;
}

interface ContainerRuleListProps {
  isLoadingContainer: boolean;
  loadContainerError?: string | null;
  container?: IContainer | Partial<IContainer> | null;
  unsavedRules: string[];
  onAddContainerRule: (rule: string) => void;
  onRemoveContainerRules: (rule: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ContainerRuleListProps;

interface State {
  entitySaved: boolean;
}

class ContainerRuleList extends BaseComponent<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { entitySaved: !this.isNew() };
  }

  public componentDidMount(): void {
    this.props.loadRulesContainer();
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.container?.containerId !== this.props.container?.containerId) {
      this.loadEntities();
    }
    if (!prevProps.container?.containerId && this.props.container?.containerId) {
      this.setState({entitySaved: true});
    }
  }

  private loadEntities = () => {
    if (this.props.container?.containerId) {
      const {containerId} = this.props.container;
      this.props.loadContainerRules(containerId);
    }
  };

  private isNew = () =>
    this.props.container?.containerId === undefined;

  private rule = (index: number, rule: string, separate: boolean, checked: boolean,
                  handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
    const isNew = this.isNew();
    const unsaved = this.props.unsavedRules.includes(rule);
    return (
      <ListItem key={index} separate={separate}>
        <div className={`${styles.linkedItemContent}`}>
          <label>
            <input id={rule}
                   type="checkbox"
                   onChange={handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
              <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                 {rule}
               </div>
            </span>
          </label>
        </div>
        {!isNew && (
          <Link to={`/rules/containers/${rule}`}
                className={`${styles.link} waves-effect`}>
            <i className={`${styles.linkIcon} material-icons right`}>link</i>
          </Link>
        )}
      </ListItem>
    );
  };

  private onAdd = (rule: string): void =>
    this.props.onAddContainerRule(rule);

  private onRemove = (rules: string[]) =>
    this.props.onRemoveContainerRules(rules);

  private onDeleteSuccess = (rules: string[]): void => {
    if (this.props.container?.containerId) {
      const {containerId} = this.props.container;
      this.props.removeContainerRules(containerId, rules);
    }
  };

  private onDeleteFailure = (reason: string): void =>
    super.toast(`Unable to delete rule`, 10000, reason, true);

  private getSelectableRules = () => {
    const {rules, rulesName, unsavedRules} = this.props;
    return Object.keys(rules).filter(name => !rulesName.includes(name) && !unsavedRules.includes(name));
  };

  public render() {
    const isNew = this.isNew();
    return <ControlledList isLoading={!isNew ? this.props.isLoadingContainer || this.props.isLoading : undefined}
                           error={!isNew ? this.props.loadContainerError || this.props.error : undefined}
                           emptyMessage={`Rules list is empty`}
                           data={this.props.rulesName}
                           dropdown={{
                             id: 'rules',
                             title: 'Add rule',
                             empty: 'No more rules to add',
                             data: this.getSelectableRules()
                           }}
                           show={this.rule}
                           onAdd={this.onAdd}
                           onRemove={this.onRemove}
                           onDelete={{
                             url: `containers/${this.props.container?.containerId}/rules`,
                             successCallback: this.onDeleteSuccess,
                             failureCallback: this.onDeleteFailure
                           }}
                           entitySaved={this.state.entitySaved}/>;
  }

}

function mapStateToProps(state: ReduxState, ownProps: ContainerRuleListProps): StateToProps {
  const containerId = ownProps.container?.containerId;
  const container = containerId && state.entities.containers.data[containerId];
  const rulesName = container && container.containerRules;
  return {
    isLoading: state.entities.containers.isLoadingRules,
    error: state.entities.containers.loadRulesError,
    rules: Object.entries(state.entities.rules.containers.data)
                 .filter(([_, rule]) => !rule.generic)
                 .map(([key, value]) => ({[key]: value}))
                 .reduce((fields, field) => {
                   for (let key in field) {
                     fields[key] = field[key];
                   }
                   return fields;
                 }, {}),
    rulesName: rulesName || [],
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadRulesContainer,
    loadContainerRules,
    removeContainerRules,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ContainerRuleList);