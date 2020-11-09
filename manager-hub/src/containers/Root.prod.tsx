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

import React from "react";
import M from "materialize-css";
import {Route, Switch} from "react-router-dom";
import LoadingBar from "react-redux-loading-bar";
import Navbar from "../views/navbar/Navbar";
import ManagementLanding from "../routes/management/landing/Landing";
import MonitoringLanding from "../routes/monitoring/landing/Landing";
import DataManagementLanding from "../routes/dataManagement/landing/Landing";
import Services from "../routes/management/services/Services";
import Service from "../routes/management/services/Service";
import {connect, Provider} from "react-redux";
import Login from "../views/login/Login";
import AuthenticatedRoute from "../components/AuthenticatedRoute";
import ManagementLogs from "../routes/management/logs/ManagementLogs";
import Region from "../routes/management/regions/Region";
import Regions from "../routes/management/regions/Regions";
import Nodes from "../routes/management/nodes/Nodes";
import Node from "../routes/management/nodes/Node";
import CloudHost from "../routes/management/hosts/cloud/CloudHost";
import EdgeHost from "../routes/management/hosts/edge/EdgeHost";
import Hosts from "../routes/management/hosts/Hosts";
import CloudHosts from "../routes/management/hosts/cloud/CloudHosts";
import EdgeHosts from "../routes/management/hosts/edge/EdgeHosts";
import Containers from "../routes/management/containers/Containers";
import Container from "../routes/management/containers/Container";
import Apps from "../routes/management/apps/Apps";
import App from "../routes/management/apps/App";
import RulesHost from "../routes/management/rules/hosts/RulesHost";
import RulesService from "../routes/management/rules/services/RulesService";
import Rules from "../routes/management/rules/Rules";
import RuleConditions from "../routes/management/rules/conditions/RuleConditions";
import Condition from "../routes/management/rules/conditions/RuleCondition";
import LoadBalancers from "../routes/management/loadBalancers/LoadBalancers";
import LoadBalancer from "../routes/management/loadBalancers/LoadBalancer";
import RegistrationServers from "../routes/management/registrationServers/RegistrationServers";
import RegistrationServer from "../routes/management/registrationServers/RegistrationServer";
import SimulatedMetrics from "../routes/management/metrics/SimulatedMetrics";
import SimulatedServiceMetric from "../routes/management/metrics/services/SimulatedServiceMetric";
import SimulatedHostMetric from "../routes/management/metrics/hosts/SimulatedHostMetric";
import SimulatedHostMetrics from "../routes/management/metrics/hosts/SimulatedHostMetrics";
import SimulatedServiceMetrics from "../routes/management/metrics/services/SimulatedServiceMetrics";
import {PageNotFound} from "../components/PageNotFound";
import {Footer} from "../views/footer/Footer";
import RulesContainer from "../routes/management/rules/containers/RulesContainer";
import RuleService from "../routes/management/rules/services/RuleService";
import RuleHost from "../routes/management/rules/hosts/RuleHost";
import RuleContainer from "../routes/management/rules/containers/RuleContainer";
import SimulatedContainerMetrics from "../routes/management/metrics/containers/SimulatedContainerMetrics";
import SimulatedContainerMetric from "../routes/management/metrics/containers/SimulatedContainerMetric";
import Ssh from "../routes/management/ssh/Ssh";
import {ReduxState} from "../reducers";
import Settings from "../routes/management/settings/Settings";
import MonitoringSettings from "../routes/monitoring/settings/Settings";
import DataManagementSettings from "../routes/dataManagement/settings/Settings";
import WorkerManager from "../routes/management/workerManagers/WorkerManager";
import WorkerManagers from "../routes/management/workerManagers/WorkerManagers";
import RuleApp from "../routes/management/rules/apps/RuleApp";
import RulesApp from "../routes/management/rules/apps/RulesApp";
import SimulatedAppMetrics from "../routes/management/metrics/apps/SimulatedAppMetrics";
import SimulatedAppMetric from "../routes/management/metrics/apps/SimulatedAppMetric";

interface RootContainerProps {
    store: any;
}

interface StateToProps {
    component: IComponent;
}

type Props = StateToProps & RootContainerProps;

export const managementAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
    "/gestor": {title: 'Gestão dinâmica de microservicos na cloud e edge', component: ManagementLanding},
    "/aplicações": {component: Apps, search: true},
    "/aplicações/:name": {component: App},
    "/serviços": {component: Services, search: true},
    "/serviços/:name": {component: Service},
    "/serviços/service": {component: Service},
    "/contentores docker": {component: Containers, search: true},
    "/contentores docker/:id": {component: Container},
    "/hosts": {component: Hosts, search: true},
    "/hosts/cloud": {component: CloudHosts},
    "/hosts/cloud/:instanceId": {component: CloudHost},
    "/hosts/edge": {component: EdgeHosts},
    "/hosts/edge/:hostname": {component: EdgeHost},
    "/nós": {component: Nodes, search: true},
    "/nós/:id": {component: Node},
    "/regras": {component: Rules, search: true},
    "/regras/hosts": {component: RulesHost, search: true},
    "/regras/hosts/:name": {component: RuleHost},
    "/regras/aplicações": {component: RulesApp, search: true},
    "/regras/aplicações/:name": {component: RuleApp},
    "/regras/serviços": {component: RulesService, search: true},
    "/regras/serviços/:name": {component: RuleService},
    "/regras/contentores": {component: RulesContainer, search: true},
    "/regras/contentores/:name": {component: RuleContainer},
    "/regras/condições": {component: RuleConditions, search: true},
    "/regras/condições/:name": {component: Condition},
    "/métricas simuladas": {component: SimulatedMetrics, search: true},
    "/métricas simuladas/hosts": {component: SimulatedHostMetrics, search: true},
    "/métricas simuladas/hosts/:name": {component: SimulatedHostMetric},
    "/métricas simuladas/aplicações": {component: SimulatedAppMetrics, search: true},
    "/métricas simuladas/aplicações/:name": {component: SimulatedAppMetric},
    "/métricas simuladas/services": {component: SimulatedServiceMetrics, search: true},
    "/métricas simuladas/services/:name": {component: SimulatedServiceMetric},
    "/métricas simuladas/contentores": {component: SimulatedContainerMetrics, search: true},
    "/métricas simuladas/contentores/:name": {component: SimulatedContainerMetric},
    "/regiões": {component: Regions, search: true},
    "/regiões/:name": {component: Region},
    "/balanceamento de carga": {component: LoadBalancers, search: true},
    "/balanceamento de carga/:id": {component: LoadBalancer},
    "/servidores de registo": {component: RegistrationServers, search: true},
    "/servidores de registo/:id": {component: RegistrationServer},
    "/gestores locais": {component: WorkerManagers, search: true},
    "/gestores locais/:id": {component: WorkerManager},
    "/secure shell": {component: Ssh},
    "/configurações": {component: Settings},
    "/logs": {component: ManagementLogs, search: true},
    "/*": {title: "404 - Not found", component: PageNotFound},
};

export const monitoringAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
    "/gestor": {title: 'Monitorização', component: MonitoringLanding},
    "/settings": {component: MonitoringSettings},
    "/*": {title: "404 - Not found", component: PageNotFound},
};

export const dataManagementAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
    "/gestor": {title: 'Dados', component: DataManagementLanding},
    "/settings": {component: DataManagementSettings},
    "/*": {title: "404 - Not found", component: PageNotFound},
};

export type IComponent = 'Gestão' | 'Monitorização' | 'Dados';

export const components: IComponent[] = [
    'Gestão', 'Monitorização', 'Dados'
];

class Root extends React.Component<Props, {}> {

    public componentDidMount(): void {
        M.AutoInit();
    }

    public render() {
        let routes = (function (component) {
            switch (component) {
                case "Gestão":
                    return managementAuthenticatedRoutes;
                case "Monitorização":
                    return monitoringAuthenticatedRoutes;
                case "Dados":
                    return dataManagementAuthenticatedRoutes;
            }
        })(this.props.component);
        return (
            <main>
                <Provider store={this.props.store}>
                    <LoadingBar showFastActions className="loading-bar"/>
                    <Navbar/>
                    <div className='main-content'>
                        <Switch>
                            <Route path="/" exact component={Login}/>
                            <Route path="/login" exact component={Login}/>
                            {Object.entries(routes).map(([path, {title, component}], index) =>
                                <AuthenticatedRoute key={index} exact path={path} title={title}
                                                    component={component}/>)}
                        </Switch>
                    </div>
                </Provider>
                <Footer/>
            </main>
        );
    }

}

function mapStateToProps(state: ReduxState): StateToProps {
    return {
        component: state.ui.component
    }
}

export default connect(mapStateToProps, undefined)(Root);