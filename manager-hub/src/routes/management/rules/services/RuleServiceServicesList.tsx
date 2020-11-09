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

import BaseComponent from "../../../../components/BaseComponent";
import ListItem from "../../../../components/list/ListItem";
import styles from "../../../../components/list/ListItem.module.css";
import React from "react";
import ControlledList from "../../../../components/list/ControlledList";
import {ReduxState} from "../../../../reducers";
import {bindActionCreators} from "redux";
import {loadRuleServices, loadServices, removeRuleServices,} from "../../../../actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";
import {IService} from "../../services/Service";
import {IRuleService} from "./RuleService";

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    ruleServices: string[];
    services: { [key: string]: IService };
}

interface DispatchToProps {
    loadServices: () => void;
    loadRuleServices: (ruleName: string) => void;
    removeRuleServices: (ruleName: string, services: string[]) => void;
}

interface ServiceRuleServicesListProps {
    isLoadingRuleService: boolean;
    loadRuleServiceError?: string | null;
    ruleService: IRuleService | Partial<IRuleService> | null;
    unsavedServices: string[];
    onAddRuleService: (service: string) => void;
    onRemoveRuleServices: (services: string[]) => void;
}

type Props = StateToProps & DispatchToProps & ServiceRuleServicesListProps

interface State {
    entitySaved: boolean;
}

class RuleServiceServicesList extends BaseComponent<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {entitySaved: !this.isNew()};
    }

    public componentDidMount(): void {
        this.props.loadServices();
        this.loadEntities();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.ruleService?.name && this.props.ruleService?.name) {
            this.setState({entitySaved: true});
        }
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList isLoading={!isNew ? this.props.isLoadingRuleService || this.props.isLoading : undefined}
                               error={!isNew ? this.props.loadRuleServiceError || this.props.error : undefined}
                               emptyMessage={`Sem serviços associadas`}
                               data={this.props.ruleServices}
                               dropdown={{
                                   id: 'services',
                                   title: 'Selecionar o serviço',
                                   empty: 'Não existe nenhum serviço disponível',
                                   data: this.getSelectableServiceNames()
                               }}
                               show={this.service}
                               onAdd={this.onAdd}
                               onRemove={this.onRemove}
                               onDelete={{
                                   url: `rules/services/${this.props.ruleService?.name}/services`,
                                   successCallback: this.onDeleteSuccess,
                                   failureCallback: this.onDeleteFailure
                               }}
                               entitySaved={this.state.entitySaved}/>;
    }

    private loadEntities = () => {
        if (this.props.ruleService?.name) {
            const {name} = this.props.ruleService;
            this.props.loadRuleServices(name);
        }
    };

    private isNew = () =>
        this.props.ruleService?.name === undefined;

    private service = (index: number, service: string, separate: boolean, checked: boolean,
                       handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void): JSX.Element => {
        const isNew = this.isNew();
        const unsaved = this.props.unsavedServices.includes(service);
        return (
            <ListItem key={index} separate={separate}>
                <div className={styles.linkedItemContent}>
                    <label>
                        <input id={service}
                               type="checkbox"
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? styles.unsavedItem : undefined}>
                                {service}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/serviços/${service}`}
                          className={`${styles.link}`}>
                        <i className={`${styles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
            </ListItem>
        );
    };

    private onAdd = (service: string): void =>
        this.props.onAddRuleService(service);

    private onRemove = (services: string[]) =>
        this.props.onRemoveRuleServices(services);

    private onDeleteSuccess = (services: string[]): void => {
        if (this.props.ruleService?.name) {
            const {name} = this.props.ruleService;
            this.props.removeRuleServices(name, services);
        }
    };

    private onDeleteFailure = (reason: string): void =>
        super.toast(`Não foi possível remover o serviço`, 10000, reason, true);

    private getSelectableServiceNames = () => {
        const {services, ruleServices, unsavedServices} = this.props;
        return Object.keys(services)
            .filter(service => !ruleServices.includes(service) && !unsavedServices.includes(service));
    };

}

function mapStateToProps(state: ReduxState, ownProps: ServiceRuleServicesListProps): StateToProps {
    const ruleName = ownProps.ruleService?.name;
    const rule = ruleName && state.entities.rules.services.data[ruleName];
    const ruleServices = rule && rule.services;
    return {
        isLoading: state.entities.rules.services.isLoadingServices,
        error: state.entities.rules.services.loadServicesError,
        ruleServices: ruleServices || [],
        services: state.entities.services.data,
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadRuleServices,
        removeRuleServices,
        loadServices,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(RuleServiceServicesList);