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
import ListItem from "../../../components/list/ListItem";
import listItemStyles from "../../../components/list/ListItem.module.css";
import styles from "../../../components/list/ListItem.module.css";
import {Link} from "react-router-dom";
import ControlledList from "../../../components/list/ControlledList";
import {ReduxState} from "../../../reducers";
import {bindActionCreators} from "redux";
import {loadAppServices, loadServices, removeAppServices,} from "../../../actions";
import {connect} from "react-redux";
import {IApp} from "./App";
import {IService} from "../services/Service";
import Field from "../../../components/form/Field";
import {IFields, IValues, requiredAndNumberAndMinAndMax} from "../../../components/form/Form";
import {IDatabaseData} from "../../../components/IData";
import {isEqual} from "lodash";
import ScrollBar from "react-perfect-scrollbar";

export interface IAppService extends IDatabaseData {
    service: IService;
    launchOrder: number;
}

export interface IAddAppService {
    service: { serviceName: string };
    launchOrder: number;
}

interface StateToProps {
    isLoading: boolean;
    error?: string | null;
    services: { [key: string]: IService };
    appServices: IAppService[];
}

interface DispatchToProps {
    loadServices: () => void;
    loadAppServices: (appName: string) => void;
    removeAppServices: (appName: string, services: string[]) => void;
}

interface AppServiceListProps {
    isLoadingApp: boolean;
    loadAppError?: string | null;
    app: IApp | Partial<IApp> | null;
    unsavedServices: IAddAppService[];
    onAddAppService: (service: IAddAppService) => void;
    onRemoveAppServices: (services: string[]) => void;
    updateAppService: (service: IAppService | IAddAppService) => void;
}

type Props = StateToProps & DispatchToProps & AppServiceListProps;

interface State {
    selectedService?: string;
    entitySaved: boolean;
    appServices: (IAppService | IAddAppService)[];
}

class AppServiceList extends BaseComponent<Props, State> {

    private controlledList = createRef<any>();
    private scrollbar = createRef<ScrollBar>();

    constructor(props: Props) {
        super(props);
        this.state = {
            entitySaved: !this.isNew(),
            appServices: [],
        };
    }

    public componentDidMount(): void {
        this.props.loadServices();
        this.loadEntities();
        this.setState({appServices: this.props.appServices});
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (!prevProps.app?.id && this.props.app?.id) {
            this.setState({entitySaved: true});
        }
        if (!isEqual(prevProps.appServices, this.props.appServices)) {
            this.setState({appServices: this.props.appServices});
        }
    }

    private updateModalScrollbar = () =>
        this.scrollbar.current?.updateScroll();

    private moveService = (position: number, service: IAddAppService | IAppService, index: number, list: (IAddAppService | IAppService)[],
                           update: (data: IAppService | IAddAppService) => void) => (_: any) => {
        const up = list[index + position];
        if (!up) {
            return;
        }
        const newLaunchOrder = Math.max(0, up.launchOrder + position);
        if (newLaunchOrder == service.launchOrder) {
            return;
        }
        let updateService = {...service, launchOrder: Math.max(0, up.launchOrder + position)};
        const updatedServices = this.state.appServices.filter(s => service.service.serviceName !== s.service.serviceName);
        updatedServices.push(updateService);
        this.setState(({appServices: updatedServices}));
        this.props.updateAppService(updateService);
        update(updateService);
    }

    public render() {
        const isNew = this.isNew();
        return <ControlledList<IAppService | IAddAppService>
            ref={this.controlledList}
            isLoading={!isNew ? this.props.isLoadingApp || this.props.isLoading : undefined}
            error={this.props.loadAppError || (isNew ? undefined : this.props.error)}
            emptyMessage={`Sem serviços associadas`}
            data={this.state.appServices}
            dataKey={['service', 'serviceName']}
            dropdown={{
                id: 'appServices',
                title: 'Selecionar o serviço',
                empty: 'Não há serviços disponíveis',
                data: this.getSelectableServicesNames(),
                onSelect: this.onDropdownSelect,
                formModal: {
                    id: 'appService',
                    fields: this.getModalFields(),
                    values: this.getModalValues(),
                    content: this.addModal,
                    position: '20%',
                    scrollbar: this.scrollbar,
                }
            }}
            show={this.service(this.controlledList?.current?.update)}
            onAddInput={this.onAdd}
            onRemove={this.onRemove}
            onDelete={{
                url: `apps/${this.props.app?.name}/services`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}
            entitySaved={this.state.entitySaved}
            sort={(a: IAppService | IAddAppService, b: IAppService | IAddAppService) => a.launchOrder - b.launchOrder}/>;
    }

    private loadEntities = () => {
        if (this.props.app?.name) {
            const {name} = this.props.app;
            this.props.loadAppServices(name);
        }
    };

    private isNew = () =>
        this.props.app?.name === undefined;

    private service = (update: (service: IAppService | IAddAppService) => void) => (index: number,
                                                                                    service: IAppService | IAddAppService,
                                                                                    separate: boolean, checked: boolean,
                                                                                    handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void,
                                                                                    list: (IAppService | IAddAppService)[]): JSX.Element => {
        const serviceName = service.service.serviceName;
        const isNew = this.isNew();
        const unsaved = this.props.unsavedServices.map(service => service.service.serviceName).includes(serviceName);
        return (
            <ListItem key={index} separate={separate}>
                <div className={`${listItemStyles.linkedItemContent}`}>
                    <label>
                        <input id={serviceName}
                               type='checkbox'
                               onChange={handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            <div className={!isNew && unsaved ? listItemStyles.unsavedItem : undefined}>
                                {service.launchOrder}. {serviceName}
                            </div>
                        </span>
                    </label>
                </div>
                {!isNew && (
                    <Link to={`/serviços/${serviceName}`}
                          className={`${listItemStyles.link}`}>
                        <i className={`${listItemStyles.linkIcon} material-icons right`}>link</i>
                    </Link>
                )}
                <div className={`${styles.actions}`}>
                    <div className={`btn-flat large white-text ${styles.actionButton}`}
                         onClick={this.moveService(-1, service, index, list, update)}>
                        <i className='small material-icons'>arrow_upward</i>
                    </div>
                    <div className={`btn-flat large white-text ${styles.actionButton}`}
                         onClick={this.moveService(1, service, index, list, update)}>
                        <i className='small material-icons'>arrow_downward</i>
                    </div>
                </div>
            </ListItem>
        );
    };

    private onAdd = (service: IValues): void => {
        this.props.onAddAppService(service as IAddAppService);
        this.setState({selectedService: undefined});
    };

    private onRemove = (services: string[]): void => {
        this.props.onRemoveAppServices(services);
    };

    private onDeleteSuccess = (services: string[]): void => {
        if (this.props.app?.name) {
            const {name} = this.props.app;
            this.props.removeAppServices(name, services);
        }
    };

    private onDeleteFailure = (reason: string, services?: string[]): void =>
        super.toast(`Não foi possível remover ${services?.length === 1 ? 'o serviço ' + services[0] : 'os serviços'} da aplicação <b>${this.props.app?.name}</b>`, 10000, reason, true);

    private getSelectableServicesNames = () => {
        const {appServices, services, unsavedServices} = this.props;
        const nonSystemServices = Object.entries(services)
            .filter(([_, value]) => value.serviceType.toLowerCase() !== 'system')
            .map(([key, _]) => key);
        const serviceNames = appServices.map(appService => appService.service.serviceName);
        const unsavedServicesNames = unsavedServices.map(service => service.service.serviceName);
        return nonSystemServices.filter(name => !serviceNames.includes(name) && !unsavedServicesNames.includes(name));
    };

    private addModal = () =>
        <>
            <Field key='launchOrder' id={'launchOrder'} label='launchOrder' type={'number'}
                   number={{min: 0, max: 2147483647}}/>
        </>;

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
            launchOrder: 0
        }
    );

    private onDropdownSelect = (selectedService: string): void => {
        this.setState({selectedService: selectedService});
    };

}

function mapStateToProps(state: ReduxState, ownProps: AppServiceListProps): StateToProps {
    const appName = ownProps.app?.name;
    const app = appName && state.entities.apps.data[appName];
    const appServices = app && app.services;
    return {
        isLoading: state.entities.apps.isLoadingServices,
        error: state.entities.apps.loadServicesError,
        services: state.entities.services.data,
        appServices: (appServices && Object.values(appServices)) || [],
    }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({
        loadServices,
        loadAppServices,
        removeAppServices,
    }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(AppServiceList);