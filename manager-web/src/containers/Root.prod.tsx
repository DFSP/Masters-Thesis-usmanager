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
import Region from "../routes/management/region/Region";
import Regions from "../routes/management/region/Regions";
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
import EurekaServers from "../routes/management/eurekaServers/EurekaServers";
import EurekaServer from "../routes/management/eurekaServers/EurekaServer";
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

interface RootContainerProps {
  store: any;
}

interface StateToProps {
  component: IComponent;
}

type Props = StateToProps & RootContainerProps;

export const managementAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
  "/home": {title: 'Microservices dynamic system management', component: ManagementLanding},
  "/apps": {component: Apps, search: true},
  "/apps/:name": {component: App},
  "/services": {component: Services, search: true},
  "/services/:name": {component: Service},
  "/services/service": {component: Service},
  "/containers": {component: Containers, search: true},
  "/containers/:id": {component: Container},
  "/hosts": {component: Hosts, search: true},
  "/hosts/cloud": {component: CloudHosts},
  "/hosts/cloud/:instanceId": {component: CloudHost},
  "/hosts/edge": {component: EdgeHosts},
  "/hosts/edge/:hostname": {component: EdgeHost},
  "/nodes": {component: Nodes, search: true},
  "/nodes/:id": {component: Node},
  "/rules": {component: Rules, search: true},
  "/rules/hosts": {component: RulesHost, search: true},
  "/rules/hosts/:name": {component: RuleHost},
  "/rules/services": {component: RulesService, search: true},
  "/rules/services/:name": {component: RuleService},
  "/rules/containers": {component: RulesContainer, search: true},
  "/rules/containers/:name": {component: RuleContainer},
  "/rules/conditions": {component: RuleConditions, search: true},
  "/rules/conditions/:name": {component: Condition},
  "/simulated-metrics": {component: SimulatedMetrics, search: true},
  "/simulated-metrics/services": {component: SimulatedServiceMetrics, search: true},
  "/simulated-metrics/services/:name?": {component: SimulatedServiceMetric},
  "/simulated-metrics/containers": {component: SimulatedContainerMetrics, search: true},
  "/simulated-metrics/containers/:name?": {component: SimulatedContainerMetric},
  "/simulated-metrics/hosts": {component: SimulatedHostMetrics, search: true},
  "/simulated-metrics/hosts/:name?": {component: SimulatedHostMetric},
  "/regions": {component: Regions, search: true},
  "/regions/:name": {component: Region},
  "/load-balancers": {component: LoadBalancers, search: true},
  "/load-balancers/:id": {component: LoadBalancer},
  "/eureka-servers": {component: EurekaServers, search: true},
  "/eureka-servers/:id": {component: EurekaServer},
  "/ssh": {component: Ssh},
  "/settings": {component: Settings},
  "/logs": {component: ManagementLogs, search: true},
  "/*": {title: "404 - Not found", component: PageNotFound},
};

export const monitoringAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
  "/home": {title: 'Microservices dynamic system monitoring', component: MonitoringLanding},
  "/settings": {component: MonitoringSettings},
  "/*": {title: "404 - Not found", component: PageNotFound},
};

export const dataAuthenticatedRoutes: { [path: string]: { title?: string, component: any, search?: boolean } } = {
  "/home": {title: 'Microservices dynamic system data management', component: DataManagementLanding},
  "/settings": {component: DataManagementSettings},
  "/*": {title: "404 - Not found", component: PageNotFound},
};

export type IComponent = 'Management' | 'Monitoring' | 'Data';

export const components: IComponent[] = [
  'Management', 'Monitoring', 'Data'
];

class Root extends React.Component<Props, {}> {

  public componentDidMount(): void {
    M.AutoInit();
  }

  public render() {
    let routes = (function (component) {
      switch (component) {
        case "Management":
          return managementAuthenticatedRoutes;
        case "Monitoring":
          return monitoringAuthenticatedRoutes;
        case "Data":
          return dataAuthenticatedRoutes;
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