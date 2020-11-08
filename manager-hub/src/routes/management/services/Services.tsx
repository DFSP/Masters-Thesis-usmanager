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

import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import ServiceCard from './ServiceCard';
import AddButton from "../../../components/form/AddButton";
import {connect} from "react-redux";
import {loadApps, loadAppServices, loadServices} from "../../../actions";
import {ReduxState} from "../../../reducers";
import CardList from "../../../components/list/CardList";
import {IService} from "./Service";
import styles from './Services.module.css'
import BaseComponent from "../../../components/BaseComponent";
import {Dropdown} from "../../../components/form/Dropdown";
import {IApp} from "../apps/App";
import { isEqual } from 'lodash';
import {Link} from "react-router-dom";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    services: IService[];
    apps: { [key: string]: IApp };
}

interface DispatchToProps {
    loadServices: (name?: string) => any;
    loadApps: () => void;
    loadAppServices: (app: string) => void;
}

type Props = StateToProps & DispatchToProps;

interface State {
    selectedApp?: string,
}

class Services extends BaseComponent<Props, State> {

    private loadedServices = false;

    state = {
        selectedApp: undefined
    }

    public componentDidMount(): void {
        this.props.loadServices();
        this.props.loadApps();
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        if (this.props.apps && !this.loadedServices) {
            Object.keys(this.props.apps).forEach(app => this.props.loadAppServices(app));
            this.loadedServices = true;
        }
    }

    private onChangeServicesFilter = (e: React.FormEvent<HTMLSelectElement>) => {
        const selectedApp = e.currentTarget.value;
        this.setState(_ => ({selectedApp}))
    }

    private filteredServices = () => {
        const {selectedApp} = this.state;
        const {services, apps} = this.props;
        return !selectedApp
            ? services
            : services.filter(service => !selectedApp
                || (selectedApp === 'System' && service.serviceType === 'SYSTEM')
                // @ts-ignore
                || Object.keys(apps[selectedApp]?.services || {})?.includes(service.serviceName))
    }

    private clearFilter = () =>
        this.setState(_ => ({selectedApp: undefined}))

    public render() {
        const {selectedApp} = this.state;
        return (
            <MainLayout>
                <AddButton button={{text: 'New service'}}
                           pathname={'/services/new_service?new'}/>
                {Object.keys(this.props.apps).length > 0 &&
                <div className={styles.filterDropdown}>
                    <a className={`btn-flat red-text ${styles.clearButton}`} onClick={this.clearFilter}>
                        <i className={`material-icons ${styles.clearButtonIcon}`}>clear</i>
                    </a>
                    <Dropdown<string>
                        id={'pageSize'}
                        name={'pageSize'}
                        value={this.state.selectedApp}
                        onChange={this.onChangeServicesFilter}
                        dropdown={{
                            defaultValue: 'Filter by app',
                            emptyMessage: 'No services to filter',
                            values: ['System', ...Object.keys(this.props.apps)],
                        }}>
                    </Dropdown>
                </div>}
                <div className={`${styles.container}`}>
                    <CardList<IService>
                        isLoading={this.props.isLoading}
                        error={this.props.error}
                        emptyMessage={selectedApp? `No services to display for ${selectedApp}` : "No services to display"}
                        list={this.filteredServices()}
                        card={this.service}
                        predicate={this.predicate}/>
                </div>
            </MainLayout>
        );
    }

    private service = (service: IService): JSX.Element =>
        <ServiceCard key={service.id} service={service}/>;

    private predicate = (service: IService, search: string): boolean =>
        service.serviceName.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.services.isLoadingServices,
        error: state.entities.services.loadServicesError,
        services: (state.entities.services.data && Object.values(state.entities.services.data).reverse()) || [],
        apps: (state.entities.apps.data && state.entities.apps.data) || {},
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadServices,
    loadApps,
    loadAppServices,
};

export default connect(mapStateToProps, mapDispatchToProps)(Services);
