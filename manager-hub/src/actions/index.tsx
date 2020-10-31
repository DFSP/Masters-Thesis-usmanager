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

import {CALL_API, Schemas} from "../middleware/api";
import {EntitiesAction} from "../reducers/entities";
import {IApp} from "../routes/management/apps/App";
import {IAddAppService} from "../routes/management/apps/AppServicesList";
import {IService} from "../routes/management/services/Service";
import {IPrediction} from "../routes/management/services/ServicePredictionList";
import {IContainer} from "../routes/management/containers/Container";
import {ICloudHost} from "../routes/management/hosts/cloud/CloudHost";
import {IEdgeHost} from "../routes/management/hosts/edge/EdgeHost";
import {INode} from "../routes/management/nodes/Node";
import {IRuleHost} from "../routes/management/rules/hosts/RuleHost";
import {IRuleService} from "../routes/management/rules/services/RuleService";
import {IRuleCondition} from "../routes/management/rules/conditions/RuleCondition";
import {ISimulatedHostMetric} from "../routes/management/metrics/hosts/SimulatedHostMetric";
import {ISimulatedServiceMetric} from "../routes/management/metrics/services/SimulatedServiceMetric";
import {ILoadBalancer} from "../routes/management/loadBalancers/LoadBalancer";
import {IRegistrationServer} from "../routes/management/registrationServers/RegistrationServer";
import {IRuleContainer} from "../routes/management/rules/containers/RuleContainer";
import {ISimulatedContainerMetric} from "../routes/management/metrics/containers/SimulatedContainerMetric";
import {IComponent} from "../containers/Root.dev";
import {IWorkerManager} from "../routes/management/workerManagers/WorkerManager";
import {IRuleApp} from "../routes/management/rules/apps/RuleApp";
import {ISimulatedAppMetric} from "../routes/management/metrics/apps/SimulatedAppMetric";
import {ISshCommand} from "../routes/management/ssh/SshCommand";
import {ICommand, IFileTransfer} from "../routes/management/ssh/Ssh";

export const APPS_REQUEST = 'APPS_REQUEST';
export const APPS_SUCCESS = 'APPS_SUCCESS';
export const APPS_FAILURE = 'APPS_FAILURE';
export const APP_REQUEST = 'APP_REQUEST';
export const APP_SUCCESS = 'APP_SUCCESS';
export const APP_FAILURE = 'APP_FAILURE';
export const loadApps = (name?: string) => (dispatch: any) => {
    return dispatch(fetchApps(name));
};
const fetchApps = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [APPS_REQUEST, APPS_SUCCESS, APPS_FAILURE],
                endpoint: `apps`,
                schema: Schemas.APP_ARRAY,
                entity: 'apps'
            }
            : {
                types: [APP_REQUEST, APP_SUCCESS, APP_FAILURE],
                endpoint: `apps/${name}`,
                schema: Schemas.APP,
                entity: 'apps'
            }
});
export const ADD_APP = 'ADD_APP';

export function addApp(app: IApp): EntitiesAction {
    return {
        type: ADD_APP,
        data: {apps: new Array(app)}
    }
}

export const UPDATE_APP = 'UPDATE_APP';

export function updateApp(previousApp: IApp, currentApp: IApp): EntitiesAction {
    return {
        type: UPDATE_APP,
        data: {apps: [previousApp, currentApp]}
    }
}

export const DELETE_APP = 'DELETE_APP';

export function deleteApp(app: IApp): EntitiesAction {
    return {
        type: DELETE_APP,
        data: {apps: [app]}
    }
}

export const APP_SERVICES_REQUEST = 'APP_SERVICES_REQUEST';
export const APP_SERVICES_SUCCESS = 'APP_SERVICES_SUCCESS';
export const APP_SERVICES_FAILURE = 'APP_SERVICES_FAILURE';
export const loadAppServices = (appName: string) => (dispatch: any) => {
    return dispatch(fetchAppServices(appName));
};
const fetchAppServices = (appName: string) => ({
    [CALL_API]: {
        types: [APP_SERVICES_REQUEST, APP_SERVICES_SUCCESS, APP_SERVICES_FAILURE],
        endpoint: `apps/${appName}/services`,
        schema: Schemas.APP_SERVICE_ARRAY,
        entity: appName
    }
});
export const ADD_APP_SERVICES = 'ADD_APP_SERVICES';

export function addAppServices(appName: string, addAppServices: IAddAppService[]): EntitiesAction {
    return {
        type: ADD_APP_SERVICES,
        entity: appName,
        data: {addAppServices: addAppServices}
    }
}

export const REMOVE_APP_SERVICES = 'REMOVE_APP_SERVICES';

export function removeAppServices(appName: string, serviceNames: string[]): EntitiesAction {
    return {
        type: REMOVE_APP_SERVICES,
        entity: appName,
        data: {serviceNames: serviceNames}
    }
}

export const APP_RULES_REQUEST = 'APP_RULES_REQUEST';
export const APP_RULES_SUCCESS = 'APP_RULES_SUCCESS';
export const APP_RULES_FAILURE = 'APP_RULES_FAILURE';
export const loadAppRules = (name: string) => (dispatch: any) => {
    return dispatch(fetchAppRules(name));
};
const fetchAppRules = (name: string) => ({
    [CALL_API]: {
        types: [APP_RULES_REQUEST, APP_RULES_SUCCESS, APP_RULES_FAILURE],
        endpoint: `apps/${name}/rules`,
        schema: Schemas.APP_RULE_ARRAY,
        entity: name
    }
});
export const ADD_APP_RULES = 'ADD_APP_RULES';

export function addAppRules(name: string, rules: string[]): EntitiesAction {
    return {
        type: ADD_APP_RULES,
        entity: name,
        data: {rulesNames: rules}
    }
}

export const REMOVE_APP_RULES = 'REMOVE_APP_RULES';

export function removeAppRules(name: string, rules: string[]): EntitiesAction {
    return {
        type: REMOVE_APP_RULES,
        entity: name,
        data: {rulesNames: rules}
    }
}

export const APP_SIMULATED_METRICS_REQUEST = 'APP_SIMULATED_METRICS_REQUEST';
export const APP_SIMULATED_METRICS_SUCCESS = 'APP_SIMULATED_METRICS_SUCCESS';
export const APP_SIMULATED_METRICS_FAILURE = 'APP_SIMULATED_METRICS_FAILURE';
export const loadAppSimulatedMetrics = (name: string) => (dispatch: any) => {
    return dispatch(fetchAppSimulatedMetrics(name));
};
const fetchAppSimulatedMetrics = (name: string) => ({
    [CALL_API]: {
        types: [APP_SIMULATED_METRICS_REQUEST, APP_SIMULATED_METRICS_SUCCESS, APP_SIMULATED_METRICS_FAILURE],
        endpoint: `apps/${name}/simulated-metrics`,
        schema: Schemas.APP_SIMULATED_METRIC_ARRAY,
        entity: name
    }
});
export const ADD_APP_SIMULATED_METRICS = 'ADD_APP_SIMULATED_METRICS';

export function addAppSimulatedMetrics(name: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: ADD_APP_SIMULATED_METRICS,
        entity: name,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const REMOVE_APP_SIMULATED_METRICS = 'REMOVE_APP_SIMULATED_METRICS';

export function removeAppSimulatedMetrics(name: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: REMOVE_APP_SIMULATED_METRICS,
        entity: name,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const SERVICES_REQUEST = 'SERVICES_REQUEST';
export const SERVICES_SUCCESS = 'SERVICES_SUCCESS';
export const SERVICES_FAILURE = 'SERVICES_FAILURE';
export const SERVICE_REQUEST = 'SERVICE_REQUEST';
export const SERVICE_SUCCESS = 'SERVICE_SUCCESS';
export const SERVICE_FAILURE = 'SERVICE_FAILURE';
export const loadServices = (name?: string) => (dispatch: any) => {
    return dispatch(fetchServices(name));
};
const fetchServices = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [SERVICES_REQUEST, SERVICES_SUCCESS, SERVICES_FAILURE],
                endpoint: `services`,
                schema: Schemas.SERVICE_ARRAY,
                entity: 'services'
            }
            : {
                types: [SERVICE_REQUEST, SERVICE_SUCCESS, SERVICE_FAILURE],
                endpoint: `services/${name}`,
                schema: Schemas.SERVICE,
                entity: 'services'
            }
});
export const ADD_SERVICE = 'ADD_SERVICE';

export function addService(service: IService): EntitiesAction {
    return {
        type: ADD_SERVICE,
        data: {services: new Array(service)}
    }
}

export const UPDATE_SERVICE = 'UPDATE_SERVICE';

export function updateService(previousService: IService, currentService: IService): EntitiesAction {
    return {
        type: UPDATE_SERVICE,
        data: {services: [previousService, currentService]}
    }
}

export const DELETE_SERVICE = 'DELETE_SERVICE';

export function deleteService(service: IService): EntitiesAction {
    return {
        type: DELETE_SERVICE,
        data: {services: [service]}
    }
}

export const SERVICE_APPS_REQUEST = 'SERVICE_APPS_REQUEST';
export const SERVICE_APPS_SUCCESS = 'SERVICE_APPS_SUCCESS';
export const SERVICE_APPS_FAILURE = 'SERVICE_APPS_FAILURE';
export const loadServiceApps = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServiceApps(serviceName));
};
const fetchServiceApps = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_APPS_REQUEST, SERVICE_APPS_SUCCESS, SERVICE_APPS_FAILURE],
        endpoint: `services/${serviceName}/apps`,
        schema: Schemas.SERVICE_APP_ARRAY,
        entity: serviceName
    }
});
export const ADD_SERVICE_APPS = 'ADD_SERVICE_APPS';

export function addServiceApps(serviceName: string, apps: string[]): EntitiesAction {
    return {
        type: ADD_SERVICE_APPS,
        entity: serviceName,
        data: {appsNames: apps}
    }
}

export const REMOVE_SERVICE_APPS = 'REMOVE_SERVICE_APPS';

export function removeServiceApps(serviceName: string, apps: string[]): EntitiesAction {
    return {
        type: REMOVE_SERVICE_APPS,
        entity: serviceName,
        data: {appsNames: apps}
    }
}

export const SERVICE_DEPENDENCIES_REQUEST = 'SERVICE_DEPENDENCIES_REQUEST';
export const SERVICE_DEPENDENCIES_SUCCESS = 'SERVICE_DEPENDENCIES_SUCCESS';
export const SERVICE_DEPENDENCIES_FAILURE = 'SERVICE_DEPENDENCIES_FAILURE';
export const loadServiceDependencies = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServiceDependencies(serviceName));
};
const fetchServiceDependencies = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_DEPENDENCIES_REQUEST, SERVICE_DEPENDENCIES_SUCCESS, SERVICE_DEPENDENCIES_FAILURE],
        endpoint: `services/${serviceName}/dependencies`,
        schema: Schemas.SERVICE_DEPENDENCY_ARRAY,
        entity: serviceName
    }
});
export const ADD_SERVICE_DEPENDENCIES = 'ADD_SERVICE_DEPENDENCIES';

export function addServiceDependencies(serviceName: string, dependencies: string[]): EntitiesAction {
    return {
        type: ADD_SERVICE_DEPENDENCIES,
        entity: serviceName,
        data: {dependenciesNames: dependencies}
    }
}

export const REMOVE_SERVICE_DEPENDENCIES = 'REMOVE_SERVICE_DEPENDENCY';

export function removeServiceDependencies(serviceName: string, dependencies: string[]): EntitiesAction {
    return {
        type: REMOVE_SERVICE_DEPENDENCIES,
        entity: serviceName,
        data: {dependenciesNames: dependencies}
    }
}

export const SERVICE_DEPENDENTS_REQUEST = 'SERVICE_DEPENDENTS_REQUEST';
export const SERVICE_DEPENDENTS_SUCCESS = 'SERVICE_DEPENDENTS_SUCCESS';
export const SERVICE_DEPENDENTS_FAILURE = 'SERVICE_DEPENDENTS_FAILURE';
export const loadServiceDependents = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServiceDependents(serviceName));
};
const fetchServiceDependents = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_DEPENDENTS_REQUEST, SERVICE_DEPENDENTS_SUCCESS, SERVICE_DEPENDENTS_FAILURE],
        endpoint: `services/${serviceName}/dependents`,
        schema: Schemas.SERVICE_DEPENDENT_ARRAY,
        entity: serviceName
    }
});

export const SERVICE_PREDICTIONS_REQUEST = 'SERVICE_PREDICTIONS_REQUEST';
export const SERVICE_PREDICTIONS_SUCCESS = 'SERVICE_PREDICTIONS_SUCCESS';
export const SERVICE_PREDICTIONS_FAILURE = 'SERVICE_PREDICTIONS_FAILURE';
export const loadServicePredictions = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServicePredictions(serviceName));
};
const fetchServicePredictions = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_PREDICTIONS_REQUEST, SERVICE_PREDICTIONS_SUCCESS, SERVICE_PREDICTIONS_FAILURE],
        endpoint: `services/${serviceName}/predictions`,
        schema: Schemas.SERVICE_PREDICTION_ARRAY,
        entity: serviceName
    }
});
export const ADD_SERVICE_PREDICTIONS = 'ADD_SERVICE_PREDICTIONS';

export function addServicePredictions(serviceName: string, predictions: IPrediction[]): EntitiesAction {
    return {
        type: ADD_SERVICE_PREDICTIONS,
        entity: serviceName,
        data: {predictions: predictions}
    }
}

export const REMOVE_SERVICE_PREDICTIONS = 'REMOVE_SERVICE_PREDICTIONS';

export function removeServicePredictions(serviceName: string, predictions: string[]): EntitiesAction {
    return {
        type: REMOVE_SERVICE_PREDICTIONS,
        entity: serviceName,
        data: {predictionsNames: predictions}
    }
}

export const SERVICE_RULES_REQUEST = 'SERVICE_RULES_REQUEST';
export const SERVICE_RULES_SUCCESS = 'SERVICE_RULES_SUCCESS';
export const SERVICE_RULES_FAILURE = 'SERVICE_RULES_FAILURE';
export const loadServiceRules = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServiceRules(serviceName));
};
const fetchServiceRules = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_RULES_REQUEST, SERVICE_RULES_SUCCESS, SERVICE_RULES_FAILURE],
        endpoint: `services/${serviceName}/rules`,
        schema: Schemas.SERVICE_RULE_ARRAY,
        entity: serviceName
    }
});
export const ADD_SERVICE_RULES = 'ADD_SERVICE_RULES';

export function addServiceRules(serviceName: string, rules: string[]): EntitiesAction {
    return {
        type: ADD_SERVICE_RULES,
        entity: serviceName,
        data: {rulesNames: rules}
    }
}

export const REMOVE_SERVICE_RULES = 'REMOVE_SERVICE_RULES';

export function removeServiceRules(serviceName: string, rules: string[]): EntitiesAction {
    return {
        type: REMOVE_SERVICE_RULES,
        entity: serviceName,
        data: {rulesNames: rules}
    }
}

export const SERVICE_SIMULATED_METRICS_REQUEST = 'SERVICE_SIMULATED_METRICS_REQUEST';
export const SERVICE_SIMULATED_METRICS_SUCCESS = 'SERVICE_SIMULATED_METRICS_SUCCESS';
export const SERVICE_SIMULATED_METRICS_FAILURE = 'SERVICE_SIMULATED_METRICS_FAILURE';
export const loadServiceSimulatedMetrics = (serviceName: string) => (dispatch: any) => {
    return dispatch(fetchServiceSimulatedMetrics(serviceName));
};
const fetchServiceSimulatedMetrics = (serviceName: string) => ({
    [CALL_API]: {
        types: [SERVICE_SIMULATED_METRICS_REQUEST, SERVICE_SIMULATED_METRICS_SUCCESS, SERVICE_SIMULATED_METRICS_FAILURE],
        endpoint: `services/${serviceName}/simulated-metrics`,
        schema: Schemas.SERVICE_SIMULATED_METRIC_ARRAY,
        entity: serviceName
    }
});
export const ADD_SERVICE_SIMULATED_METRICS = 'ADD_SERVICE_SIMULATED_METRICS';

export function addServiceSimulatedMetrics(serviceName: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: ADD_SERVICE_SIMULATED_METRICS,
        entity: serviceName,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const REMOVE_SERVICE_SIMULATED_METRICS = 'REMOVE_SERVICE_SIMULATED_METRICS';

export function removeServiceSimulatedMetrics(serviceName: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: REMOVE_SERVICE_SIMULATED_METRICS,
        entity: serviceName,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const CONTAINERS_REQUEST = 'CONTAINERS_REQUEST';
export const CONTAINERS_SUCCESS = 'CONTAINERS_SUCCESS';
export const CONTAINERS_FAILURE = 'CONTAINERS_FAILURE';
export const CONTAINER_REQUEST = 'CONTAINER_REQUEST';
export const CONTAINER_SUCCESS = 'CONTAINER_SUCCESS';
export const CONTAINER_FAILURE = 'CONTAINER_FAILURE';
export const loadContainers = (id?: string) => (dispatch: any) => {
    return dispatch(fetchContainers(id));
};
const fetchContainers = (id?: string) => ({
    [CALL_API]:
        !id
            ? {
                types: [CONTAINERS_REQUEST, CONTAINERS_SUCCESS, CONTAINERS_FAILURE],
                endpoint: `containers`,
                schema: Schemas.CONTAINER_ARRAY,
                entity: 'containers'
            }
            : {
                types: [CONTAINER_REQUEST, CONTAINER_SUCCESS, CONTAINER_FAILURE],
                endpoint: `containers/${id}`,
                schema: Schemas.CONTAINER,
                entity: 'containers'
            }
});
export const ADD_CONTAINER = 'ADD_CONTAINER';

export function addContainer(container: IContainer): EntitiesAction {
    return {
        type: ADD_CONTAINER,
        data: {containers: new Array(container)}
    }
}

export const DELETE_CONTAINER = 'DELETE_CONTAINER';

export function deleteContainer(container: IContainer): EntitiesAction {
    return {
        type: DELETE_CONTAINER,
        data: {containers: [container]}
    }
}

export const reloadContainers = () => ({
    [CALL_API]: {
        types: [CONTAINERS_REQUEST, CONTAINERS_SUCCESS, CONTAINERS_FAILURE],
        endpoint: `containers/reload`,
        schema: Schemas.CONTAINER_ARRAY,
        entity: 'containers',
        method: 'post'
    }
});

export const CONTAINER_LOGS_REQUEST = 'CONTAINER_LOGS_REQUEST';
export const CONTAINER_LOGS_SUCCESS = 'CONTAINER_LOGS_SUCCESS';
export const CONTAINER_LOGS_FAILURE = 'CONTAINER_LOGS_FAILURE';
export const loadContainerLogs = (containerId: string) => ({
    [CALL_API]: {
        types: [CONTAINER_LOGS_REQUEST, CONTAINER_LOGS_SUCCESS, CONTAINER_LOGS_FAILURE],
        endpoint: `containers/${containerId}/logs`,
        entity: containerId,
    }
});

export const CONTAINER_RULES_REQUEST = 'CONTAINER_RULES_REQUEST';
export const CONTAINER_RULES_SUCCESS = 'CONTAINER_RULES_SUCCESS';
export const CONTAINER_RULES_FAILURE = 'CONTAINER_RULES_FAILURE';
export const loadContainerRules = (containerId: string) => (dispatch: any) => {
    return dispatch(fetchContainerRules(containerId));
};
const fetchContainerRules = (containerId: string) => ({
    [CALL_API]: {
        types: [CONTAINER_RULES_REQUEST, CONTAINER_RULES_SUCCESS, CONTAINER_RULES_FAILURE],
        endpoint: `containers/${containerId}/rules`,
        schema: Schemas.CONTAINER_RULE_ARRAY,
        entity: containerId
    }
});
export const ADD_CONTAINER_RULES = 'ADD_CONTAINER_RULES';

export function addContainerRules(containerId: string, rules: string[]): EntitiesAction {
    return {
        type: ADD_CONTAINER_RULES,
        entity: containerId,
        data: {rulesNames: rules}
    }
}

export const REMOVE_CONTAINER_RULES = 'REMOVE_CONTAINER_RULES';

export function removeContainerRules(containerId: string, rules: string[]): EntitiesAction {
    return {
        type: REMOVE_CONTAINER_RULES,
        entity: containerId,
        data: {rulesNames: rules}
    }
}

export const CONTAINER_SIMULATED_METRICS_REQUEST = 'CONTAINER_SIMULATED_METRICS_REQUEST';
export const CONTAINER_SIMULATED_METRICS_SUCCESS = 'CONTAINER_SIMULATED_METRICS_SUCCESS';
export const CONTAINER_SIMULATED_METRICS_FAILURE = 'CONTAINER_SIMULATED_METRICS_FAILURE';
export const loadContainerSimulatedMetrics = (containerId: string) => (dispatch: any) => {
    return dispatch(fetchContainerSimulatedMetrics(containerId));
};
const fetchContainerSimulatedMetrics = (containerId: string) => ({
    [CALL_API]: {
        types: [CONTAINER_SIMULATED_METRICS_REQUEST, CONTAINER_SIMULATED_METRICS_SUCCESS, CONTAINER_SIMULATED_METRICS_FAILURE],
        endpoint: `containers/${containerId}/simulated-metrics`,
        schema: Schemas.CONTAINER_SIMULATED_METRIC_ARRAY,
        entity: containerId
    }
});
export const ADD_CONTAINER_SIMULATED_METRICS = 'ADD_CONTAINER_SIMULATED_METRICS';

export function addContainerSimulatedMetrics(containerId: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: ADD_CONTAINER_SIMULATED_METRICS,
        entity: containerId,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const REMOVE_CONTAINER_SIMULATED_METRICS = 'REMOVE_CONTAINER_SIMULATED_METRICS';

export function removeContainerSimulatedMetrics(containerId: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: REMOVE_CONTAINER_SIMULATED_METRICS,
        entity: containerId,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const CLOUD_HOSTS_REQUEST = 'CLOUD_HOSTS_REQUEST';
export const CLOUD_HOSTS_SUCCESS = 'CLOUD_HOSTS_SUCCESS';
export const CLOUD_HOSTS_FAILURE = 'CLOUD_HOSTS_FAILURE';
export const CLOUD_HOST_REQUEST = 'CLOUD_HOST_REQUEST';
export const CLOUD_HOST_SUCCESS = 'CLOUD_HOST_SUCCESS';
export const CLOUD_HOST_FAILURE = 'CLOUD_HOST_FAILURE';
export const loadCloudHosts = (instanceId?: string) => (dispatch: any) => {
    return dispatch(fetchCloudHosts(instanceId));
};
const fetchCloudHosts = (instanceId?: string) => ({
    [CALL_API]:
        !instanceId
            ? {
                types: [CLOUD_HOSTS_REQUEST, CLOUD_HOSTS_SUCCESS, CLOUD_HOSTS_FAILURE],
                endpoint: `hosts/cloud`,
                schema: Schemas.CLOUD_HOST_ARRAY,
                entity: 'cloudHosts'
            }
            : {
                types: [CLOUD_HOST_REQUEST, CLOUD_HOST_SUCCESS, CLOUD_HOST_FAILURE],
                endpoint: `hosts/cloud/${instanceId}`,
                schema: Schemas.CLOUD_HOST,
                entity: 'cloudHosts'
            }
});

export const syncCloudHosts = () => ({
    [CALL_API]: {
        types: [CLOUD_HOSTS_REQUEST, CLOUD_HOSTS_SUCCESS, CLOUD_HOSTS_FAILURE],
        endpoint: `hosts/cloud/sync`,
        schema: Schemas.CLOUD_HOST_ARRAY,
        entity: 'cloudHosts',
        method: 'post'
    }
});

export const ADD_CLOUD_HOST = 'ADD_CLOUD_HOST';

export function addCloudHost(cloudHost: ICloudHost): EntitiesAction {
    return {
        type: ADD_CLOUD_HOST,
        data: {cloudHosts: new Array(cloudHost)}
    }
}

export const UPDATE_CLOUD_HOST = 'UPDATE_CLOUD_HOST';

export function updateCloudHost(previousCloudHost: ICloudHost, currentCloudHost: ICloudHost): EntitiesAction {
    return {
        type: UPDATE_CLOUD_HOST,
        data: {cloudHosts: [previousCloudHost, currentCloudHost]}
    }
}

export const DELETE_CLOUD_HOST = 'DELETE_CLOUD_HOST';

export function deleteCloudHost(cloudHost: ICloudHost): EntitiesAction {
    return {
        type: DELETE_CLOUD_HOST,
        data: {cloudHosts: [cloudHost]}
    }
}

export const CLOUD_HOST_RULES_REQUEST = 'CLOUD_HOST_RULES_REQUEST';
export const CLOUD_HOST_RULES_SUCCESS = 'CLOUD_HOST_RULES_SUCCESS';
export const CLOUD_HOST_RULES_FAILURE = 'CLOUD_HOST_RULES_FAILURE';
export const loadCloudHostRules = (instanceId: string) => (dispatch: any) => {
    return dispatch(fetchCloudHostRules(instanceId));
};
const fetchCloudHostRules = (cloudHost: string) => ({
    [CALL_API]: {
        types: [CLOUD_HOST_RULES_REQUEST, CLOUD_HOST_RULES_SUCCESS, CLOUD_HOST_RULES_FAILURE],
        endpoint: `hosts/cloud/${cloudHost}/rules`,
        schema: Schemas.CLOUD_HOST_RULE_ARRAY,
        entity: cloudHost
    }
});
export const ADD_CLOUD_HOST_RULE = 'ADD_CLOUD_HOST_RULE';

export function addCloudHostRule(cloudHost: string, rule: string): EntitiesAction {
    return {
        type: ADD_CLOUD_HOST_RULE,
        entity: cloudHost,
        data: {rulesNames: new Array(rule)}
    }
}

export const REMOVE_CLOUD_HOST_RULES = 'REMOVE_CLOUD_HOST_RULES';

export function removeCloudHostRules(cloudHost: string, rules: string[]): EntitiesAction {
    return {
        type: REMOVE_CLOUD_HOST_RULES,
        entity: cloudHost,
        data: {rulesNames: rules}
    }
}

export const CLOUD_HOST_SIMULATED_METRICS_REQUEST = 'CLOUD_HOST_SIMULATED_METRICS_REQUEST';
export const CLOUD_HOST_SIMULATED_METRICS_SUCCESS = 'CLOUD_HOST_SIMULATED_METRICS_SUCCESS';
export const CLOUD_HOST_SIMULATED_METRICS_FAILURE = 'CLOUD_HOST_SIMULATED_METRICS_FAILURE';
export const loadCloudHostSimulatedMetrics = (cloudHost: string) => (dispatch: any) => {
    return dispatch(fetchCloudHostSimulatedMetrics(cloudHost));
};
const fetchCloudHostSimulatedMetrics = (cloudHost: string) => ({
    [CALL_API]: {
        types: [CLOUD_HOST_SIMULATED_METRICS_REQUEST, CLOUD_HOST_SIMULATED_METRICS_SUCCESS, CLOUD_HOST_SIMULATED_METRICS_FAILURE],
        endpoint: `hosts/cloud/${cloudHost}/simulated-metrics`,
        schema: Schemas.CLOUD_HOST_SIMULATED_METRIC_ARRAY,
        entity: cloudHost
    }
});
export const ADD_CLOUD_HOST_SIMULATED_METRICS = 'ADD_CLOUD_HOST_SIMULATED_METRICS';

export function addCloudHostSimulatedMetrics(cloudHost: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: ADD_CLOUD_HOST_SIMULATED_METRICS,
        entity: cloudHost,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const REMOVE_CLOUD_HOST_SIMULATED_METRICS = 'REMOVE_CLOUD_HOST_SIMULATED_METRICS';

export function removeCloudHostSimulatedMetrics(cloudHost: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: REMOVE_CLOUD_HOST_SIMULATED_METRICS,
        entity: cloudHost,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const CLOUD_REGIONS_REQUEST = 'CLOUD_REGIONS_REQUEST';
export const CLOUD_REGIONS_SUCCESS = 'CLOUD_REGIONS_SUCCESS';
export const CLOUD_REGIONS_FAILURE = 'CLOUD_REGIONS_FAILURE';
export const loadCloudRegions = () => (dispatch: any) => {
    return dispatch(fetchCloudRegions());
};
const fetchCloudRegions = () => ({
    [CALL_API]: {
        types: [CLOUD_REGIONS_REQUEST, CLOUD_REGIONS_SUCCESS, CLOUD_REGIONS_FAILURE],
        endpoint: `hosts/cloud/regions`,
    }
});

export const EDGE_HOSTS_REQUEST = 'EDGE_HOSTS_REQUEST';
export const EDGE_HOSTS_SUCCESS = 'EDGE_HOSTS_SUCCESS';
export const EDGE_HOSTS_FAILURE = 'EDGE_HOSTS_FAILURE';
export const EDGE_HOST_REQUEST = 'EDGE_HOST_REQUEST';
export const EDGE_HOST_SUCCESS = 'EDGE_HOST_SUCCESS';
export const EDGE_HOST_FAILURE = 'EDGE_HOST_FAILURE';
export const loadEdgeHosts = (hostname?: string) => (dispatch: any) => {
    return dispatch(fetchEdgeHosts(hostname));
};
const fetchEdgeHosts = (hostname?: string) => ({
    [CALL_API]:
        !hostname
            ? {
                types: [EDGE_HOSTS_REQUEST, EDGE_HOSTS_SUCCESS, EDGE_HOSTS_FAILURE],
                endpoint: `hosts/edge`,
                schema: Schemas.EDGE_HOST_ARRAY,
                entity: 'edgeHosts'
            }
            : {
                types: [EDGE_HOST_REQUEST, EDGE_HOST_SUCCESS, EDGE_HOST_FAILURE],
                endpoint: `hosts/edge/${hostname}`,
                schema: Schemas.EDGE_HOST,
                entity: 'edgeHosts'
            }
});
export const ADD_EDGE_HOST = 'ADD_EDGE_HOST';

export function addEdgeHost(edgeHost: IEdgeHost): EntitiesAction {
    return {
        type: ADD_EDGE_HOST,
        data: {edgeHosts: new Array(edgeHost)}
    }
}

export const UPDATE_EDGE_HOST = 'UPDATE_EDGE_HOST';

export function updateEdgeHost(previousEdgeHost: IEdgeHost, currentEdgeHost: IEdgeHost): EntitiesAction {
    return {
        type: UPDATE_EDGE_HOST,
        data: {edgeHosts: [previousEdgeHost, currentEdgeHost]}
    }
}

export const DELETE_EDGE_HOST = 'DELETE_EDGE_HOST';

export function deleteEdgeHost(edgeHost: IEdgeHost): EntitiesAction {
    return {
        type: DELETE_EDGE_HOST,
        data: {edgeHosts: [edgeHost]}
    }
}

export const EDGE_HOST_RULES_REQUEST = 'EDGE_HOST_RULES_REQUEST';
export const EDGE_HOST_RULES_SUCCESS = 'EDGE_HOST_RULES_SUCCESS';
export const EDGE_HOST_RULES_FAILURE = 'EDGE_HOST_RULES_FAILURE';
export const loadEdgeHostRules = (hostname: string) => (dispatch: any) => {
    return dispatch(fetchEdgeHostRules(hostname));
};
const fetchEdgeHostRules = (hostname: string) => ({
    [CALL_API]: {
        types: [EDGE_HOST_RULES_REQUEST, EDGE_HOST_RULES_SUCCESS, EDGE_HOST_RULES_FAILURE],
        endpoint: `hosts/edge/${hostname}/rules`,
        schema: Schemas.EDGE_HOST_RULE_ARRAY,
        entity: hostname
    }
});
export const ADD_EDGE_HOST_RULES = 'ADD_EDGE_HOST_RULES';

export function addEdgeHostRules(hostname: string, rules: string[]): EntitiesAction {
    return {
        type: ADD_EDGE_HOST_RULES,
        entity: hostname,
        data: {rulesNames: rules}
    }
}

export const REMOVE_EDGE_HOST_RULES = 'REMOVE_EDGE_HOST_RULES';

export function removeEdgeHostRules(hostname: string, rules: string[]): EntitiesAction {
    return {
        type: REMOVE_EDGE_HOST_RULES,
        entity: hostname,
        data: {rulesNames: rules}
    }
}

export const EDGE_HOST_SIMULATED_METRICS_REQUEST = 'EDGE_HOST_SIMULATED_METRICS_REQUEST';
export const EDGE_HOST_SIMULATED_METRICS_SUCCESS = 'EDGE_HOST_SIMULATED_METRICS_SUCCESS';
export const EDGE_HOST_SIMULATED_METRICS_FAILURE = 'EDGE_HOST_SIMULATED_METRICS_FAILURE';
export const loadEdgeHostSimulatedMetrics = (hostname: string) => (dispatch: any) => {
    return dispatch(fetchEdgeHostSimulatedMetrics(hostname));
};
const fetchEdgeHostSimulatedMetrics = (hostname: string) => ({
    [CALL_API]: {
        types: [EDGE_HOST_SIMULATED_METRICS_REQUEST, EDGE_HOST_SIMULATED_METRICS_SUCCESS, EDGE_HOST_SIMULATED_METRICS_FAILURE],
        endpoint: `hosts/edge/${hostname}/simulated-metrics`,
        schema: Schemas.EDGE_HOST_SIMULATED_METRIC_ARRAY,
        entity: hostname
    }
});
export const ADD_EDGE_HOST_SIMULATED_METRICS = 'ADD_EDGE_HOST_SIMULATED_METRICS';

export function addEdgeHostSimulatedMetrics(hostname: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: ADD_EDGE_HOST_SIMULATED_METRICS,
        entity: hostname,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const REMOVE_EDGE_HOST_SIMULATED_METRICS = 'REMOVE_EDGE_HOST_SIMULATED_METRICS';

export function removeEdgeHostSimulatedMetrics(hostname: string, simulatedMetrics: string[]): EntitiesAction {
    return {
        type: REMOVE_EDGE_HOST_SIMULATED_METRICS,
        entity: hostname,
        data: {simulatedMetricNames: simulatedMetrics}
    }
}

export const NODES_REQUEST = 'NODES_REQUEST';
export const NODES_SUCCESS = 'NODES_SUCCESS';
export const NODES_FAILURE = 'NODES_FAILURE';
export const NODE_REQUEST = 'NODE_REQUEST';
export const NODE_SUCCESS = 'NODE_SUCCESS';
export const NODE_FAILURE = 'NODE_FAILURE';
export const loadNodes = (id?: string) => (dispatch: any) => {
    return dispatch(fetchNodes(id));
};
const fetchNodes = (id?: string) => ({
    [CALL_API]:
        !id
            ? {
                types: [NODES_REQUEST, NODES_SUCCESS, NODES_FAILURE],
                endpoint: `nodes`,
                schema: Schemas.NODE_ARRAY,
                entity: 'nodes'
            }
            : {
                types: [NODE_REQUEST, NODE_SUCCESS, NODE_FAILURE],
                endpoint: `nodes/${id}`,
                schema: Schemas.NODE,
                entity: 'nodes'
            }
});
export const ADD_NODE = 'ADD_NODE';

export function addNode(node: INode): EntitiesAction {
    return {
        type: ADD_NODE,
        data: {nodes: new Array(node)}
    }
}

export const UPDATE_NODE = 'UPDATE_NODE';

export function updateNode(previousNode: INode, currentNode: INode): EntitiesAction {
    return {
        type: UPDATE_NODE,
        data: {nodes: [previousNode, currentNode]}
    }
}

export const DELETE_NODE = 'DELETE_NODE';

export function deleteNode(node: INode): EntitiesAction {
    return {
        type: DELETE_NODE,
        data: {nodes: [node]}
    }
}

export const RULES_HOST_REQUEST = 'RULES_HOST_REQUEST';
export const RULES_HOST_SUCCESS = 'RULES_HOST_SUCCESS';
export const RULES_HOST_FAILURE = 'RULES_HOST_FAILURE';
export const RULE_HOST_REQUEST = 'RULE_HOST_REQUEST';
export const RULE_HOST_SUCCESS = 'RULE_HOST_SUCCESS';
export const RULE_HOST_FAILURE = 'RULE_HOST_FAILURE';
export const loadRulesHost = (name?: string) => (dispatch: any) => {
    return dispatch(fetchRulesHost(name));
};
const fetchRulesHost = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [RULES_HOST_REQUEST, RULES_HOST_SUCCESS, RULES_HOST_FAILURE],
                endpoint: `rules/hosts`,
                schema: Schemas.RULE_HOST_ARRAY,
                entity: 'hostRules'
            }
            : {
                types: [RULE_HOST_REQUEST, RULE_HOST_SUCCESS, RULE_HOST_FAILURE],
                endpoint: `rules/hosts/${name}`,
                schema: Schemas.RULE_HOST,
                entity: 'hostRules'
            }
});
export const ADD_RULE_HOST = 'ADD_RULE_HOST';

export function addRuleHost(ruleHost: IRuleHost): EntitiesAction {
    return {
        type: ADD_RULE_HOST,
        data: {hostRules: new Array(ruleHost)}
    }
}

export const UPDATE_RULE_HOST = 'UPDATE_RULE_HOST';

export function updateRuleHost(previousRuleHost: IRuleHost, currentRuleHost: IRuleHost): EntitiesAction {
    return {
        type: UPDATE_RULE_HOST,
        data: {hostRules: [previousRuleHost, currentRuleHost]}
    }
}

export const DELETE_RULE_HOST = 'DELETE_RULE_HOST';

export function deleteHostRule(hostRule: IRuleHost): EntitiesAction {
    return {
        type: DELETE_RULE_HOST,
        data: {hostRules: [hostRule]}
    }
}

export const RULE_HOST_CONDITIONS_REQUEST = 'RULE_HOST_CONDITIONS_REQUEST';
export const RULE_HOST_CONDITIONS_SUCCESS = 'RULE_HOST_CONDITIONS_SUCCESS';
export const RULE_HOST_CONDITIONS_FAILURE = 'RULE_HOST_CONDITIONS_FAILURE';
export const loadRuleHostConditions = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleHostConditions(ruleName));
};
const fetchRuleHostConditions = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_HOST_CONDITIONS_REQUEST, RULE_HOST_CONDITIONS_SUCCESS, RULE_HOST_CONDITIONS_FAILURE],
        endpoint: `rules/hosts/${ruleName}/conditions`,
        schema: Schemas.RULE_CONDITION_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_HOST_CONDITIONS = 'ADD_RULE_HOST_CONDITIONS';

export function addRuleHostConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: ADD_RULE_HOST_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const REMOVE_RULE_HOST_CONDITIONS = 'REMOVE_RULE_HOST_CONDITIONS';

export function removeRuleHostConditions(ruleName: string, condition: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_HOST_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: condition}
    }
}

export const RULE_HOST_CLOUD_HOSTS_REQUEST = 'RULE_HOST_CLOUD_HOSTS_REQUEST';
export const RULE_HOST_CLOUD_HOSTS_SUCCESS = 'RULE_HOST_CLOUD_HOSTS_SUCCESS';
export const RULE_HOST_CLOUD_HOSTS_FAILURE = 'RULE_HOST_CLOUD_HOSTS_FAILURE';
export const loadRuleHostCloudHosts = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleHostCloudHosts(ruleName));
};
const fetchRuleHostCloudHosts = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_HOST_CLOUD_HOSTS_REQUEST, RULE_HOST_CLOUD_HOSTS_SUCCESS, RULE_HOST_CLOUD_HOSTS_FAILURE],
        endpoint: `rules/hosts/${ruleName}/cloud-hosts`,
        schema: Schemas.CLOUD_HOST_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_HOST_CLOUD_HOSTS = 'ADD_RULE_HOST_CLOUD_HOSTS';

export function addRuleCloudHosts(ruleName: string, cloudHosts: string[]): EntitiesAction {
    return {
        type: ADD_RULE_HOST_CLOUD_HOSTS,
        entity: ruleName,
        data: {cloudHostsId: cloudHosts}
    }
}

export const REMOVE_RULE_HOST_CLOUD_HOSTS = 'REMOVE_RULE_HOST_CLOUD_HOSTS';

export function removeRuleHostCloudHosts(ruleName: string, cloudHost: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_HOST_CLOUD_HOSTS,
        entity: ruleName,
        data: {cloudHostsId: cloudHost}
    }
}

export const RULE_HOST_EDGE_HOSTS_REQUEST = 'RULE_HOST_EDGE_HOSTS_REQUEST';
export const RULE_HOST_EDGE_HOSTS_SUCCESS = 'RULE_HOST_EDGE_HOSTS_SUCCESS';
export const RULE_HOST_EDGE_HOSTS_FAILURE = 'RULE_HOST_EDGE_HOSTS_FAILURE';
export const loadRuleHostEdgeHosts = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleHostEdgeHosts(ruleName));
};
const fetchRuleHostEdgeHosts = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_HOST_EDGE_HOSTS_REQUEST, RULE_HOST_EDGE_HOSTS_SUCCESS, RULE_HOST_EDGE_HOSTS_FAILURE],
        endpoint: `rules/hosts/${ruleName}/edge-hosts`,
        schema: Schemas.EDGE_HOST_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_HOST_EDGE_HOSTS = 'ADD_RULE_HOST_EDGE_HOSTS';

export function addRuleEdgeHosts(ruleName: string, edgeHosts: string[]): EntitiesAction {
    return {
        type: ADD_RULE_HOST_EDGE_HOSTS,
        entity: ruleName,
        data: {edgeHostsHostname: edgeHosts}
    }
}

export const REMOVE_RULE_HOST_EDGE_HOSTS = 'REMOVE_RULE_HOST_EDGE_HOSTS';

export function removeRuleHostEdgeHosts(ruleName: string, edgeHosts: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_HOST_EDGE_HOSTS,
        entity: ruleName,
        data: {edgeHostsHostname: edgeHosts}
    }
}

export const RULES_APP_REQUEST = 'RULES_APP_REQUEST';
export const RULES_APP_SUCCESS = 'RULES_APP_SUCCESS';
export const RULES_APP_FAILURE = 'RULES_APP_FAILURE';
export const RULE_APP_REQUEST = 'RULE_APP_REQUEST';
export const RULE_APP_SUCCESS = 'RULE_APP_SUCCESS';
export const RULE_APP_FAILURE = 'RULE_APP_FAILURE';
export const loadRulesApp = (name?: string) => (dispatch: any) => {
    return dispatch(fetchRulesApp(name));
};
const fetchRulesApp = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [RULES_APP_REQUEST, RULES_APP_SUCCESS, RULES_APP_FAILURE],
                endpoint: `rules/apps`,
                schema: Schemas.RULE_APP_ARRAY,
                entity: 'rules'
            }
            : {
                types: [RULE_APP_REQUEST, RULE_APP_SUCCESS, RULE_APP_FAILURE],
                endpoint: `rules/apps/${name}`,
                schema: Schemas.RULE_APP,
                entity: 'rules'
            }
});
export const ADD_RULE_APP = 'ADD_RULE_APP';

export function addRuleApp(ruleApp: IRuleApp): EntitiesAction {
    return {
        type: ADD_RULE_APP,
        data: {appRules: new Array(ruleApp)}
    }
}

export const UPDATE_RULE_APP = 'UPDATE_RULE_APP';

export function updateRuleApp(previousRuleApp: IRuleApp, currentRuleApp: IRuleApp): EntitiesAction {
    return {
        type: UPDATE_RULE_APP,
        data: {appRules: [previousRuleApp, currentRuleApp]}
    }
}

export const DELETE_RULE_APP = 'DELETE_RULE_APP';

export function deleteAppRule(appRule: IRuleApp): EntitiesAction {
    return {
        type: DELETE_RULE_APP,
        data: {appRules: [appRule]}
    }
}

export const RULE_APP_CONDITIONS_REQUEST = 'RULE_APP_CONDITIONS_REQUEST';
export const RULE_APP_CONDITIONS_SUCCESS = 'RULE_APP_CONDITIONS_SUCCESS';
export const RULE_APP_CONDITIONS_FAILURE = 'RULE_APP_CONDITIONS_FAILURE';
export const loadRuleAppConditions = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleAppConditions(ruleName));
};
const fetchRuleAppConditions = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_APP_CONDITIONS_REQUEST, RULE_APP_CONDITIONS_SUCCESS, RULE_APP_CONDITIONS_FAILURE],
        endpoint: `rules/apps/${ruleName}/conditions`,
        schema: Schemas.RULE_CONDITION_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_APP_CONDITIONS = 'ADD_RULE_APP_CONDITIONS';

export function addRuleAppConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: ADD_RULE_APP_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const REMOVE_RULE_APP_CONDITIONS = 'REMOVE_RULE_APP_CONDITIONS';

export function removeRuleAppConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_APP_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const RULE_APP_APPS_REQUEST = 'RULE_APP_APPS_REQUEST';
export const RULE_APP_APPS_SUCCESS = 'RULE_APP_APPS_SUCCESS';
export const RULE_APP_APPS_FAILURE = 'RULE_APP_APPS_FAILURE';
export const loadRuleApps = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleAppApps(ruleName));
};
const fetchRuleAppApps = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_APP_APPS_REQUEST, RULE_APP_APPS_SUCCESS, RULE_APP_APPS_FAILURE],
        endpoint: `rules/apps/${ruleName}/apps`,
        schema: Schemas.APP_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_APP_APPS = 'ADD_RULE_APP_APPS';

export function addRuleApps(ruleName: string, apps: string[]): EntitiesAction {
    return {
        type: ADD_RULE_APP_APPS,
        entity: ruleName,
        data: {appsNames: apps}
    }
}

export const REMOVE_RULE_APP_APPS = 'REMOVE_RULE_APP_APPS';

export function removeRuleApps(ruleName: string, apps: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_APP_APPS,
        entity: ruleName,
        data: {appsNames: apps}
    }
}

export const RULES_SERVICE_REQUEST = 'RULES_SERVICE_REQUEST';
export const RULES_SERVICE_SUCCESS = 'RULES_SERVICE_SUCCESS';
export const RULES_SERVICE_FAILURE = 'RULES_SERVICE_FAILURE';
export const RULE_SERVICE_REQUEST = 'RULE_SERVICE_REQUEST';
export const RULE_SERVICE_SUCCESS = 'RULE_SERVICE_SUCCESS';
export const RULE_SERVICE_FAILURE = 'RULE_SERVICE_FAILURE';
export const loadRulesService = (name?: string) => (dispatch: any) => {
    return dispatch(fetchRulesService(name));
};
const fetchRulesService = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [RULES_SERVICE_REQUEST, RULES_SERVICE_SUCCESS, RULES_SERVICE_FAILURE],
                endpoint: `rules/services`,
                schema: Schemas.RULE_SERVICE_ARRAY,
                entity: 'rules'
            }
            : {
                types: [RULE_SERVICE_REQUEST, RULE_SERVICE_SUCCESS, RULE_SERVICE_FAILURE],
                endpoint: `rules/services/${name}`,
                schema: Schemas.RULE_SERVICE,
                entity: 'rules'
            }
});
export const ADD_RULE_SERVICE = 'ADD_RULE_SERVICE';

export function addRuleService(ruleService: IRuleService): EntitiesAction {
    return {
        type: ADD_RULE_SERVICE,
        data: {serviceRules: new Array(ruleService)}
    }
}

export const UPDATE_RULE_SERVICE = 'UPDATE_RULE_SERVICE';

export function updateRuleService(previousRuleService: IRuleService, currentRuleService: IRuleService): EntitiesAction {
    return {
        type: UPDATE_RULE_SERVICE,
        data: {serviceRules: [previousRuleService, currentRuleService]}
    }
}

export const DELETE_RULE_SERVICE = 'DELETE_RULE_SERVICE';

export function deleteServiceRule(serviceRule: IRuleService): EntitiesAction {
    return {
        type: DELETE_RULE_SERVICE,
        data: {serviceRules: [serviceRule]}
    }
}

export const RULE_SERVICE_CONDITIONS_REQUEST = 'RULE_SERVICE_CONDITIONS_REQUEST';
export const RULE_SERVICE_CONDITIONS_SUCCESS = 'RULE_SERVICE_CONDITIONS_SUCCESS';
export const RULE_SERVICE_CONDITIONS_FAILURE = 'RULE_SERVICE_CONDITIONS_FAILURE';
export const loadRuleServiceConditions = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleServiceConditions(ruleName));
};
const fetchRuleServiceConditions = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_SERVICE_CONDITIONS_REQUEST, RULE_SERVICE_CONDITIONS_SUCCESS, RULE_SERVICE_CONDITIONS_FAILURE],
        endpoint: `rules/services/${ruleName}/conditions`,
        schema: Schemas.RULE_CONDITION_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_SERVICE_CONDITIONS = 'ADD_RULE_SERVICE_CONDITIONS';

export function addRuleServiceConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: ADD_RULE_SERVICE_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const REMOVE_RULE_SERVICE_CONDITIONS = 'REMOVE_RULE_SERVICE_CONDITIONS';

export function removeRuleServiceConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_SERVICE_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const RULE_SERVICE_SERVICES_REQUEST = 'RULE_SERVICE_SERVICES_REQUEST';
export const RULE_SERVICE_SERVICES_SUCCESS = 'RULE_SERVICE_SERVICES_SUCCESS';
export const RULE_SERVICE_SERVICES_FAILURE = 'RULE_SERVICE_SERVICES_FAILURE';
export const loadRuleServices = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleServices(ruleName));
};
const fetchRuleServices = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_SERVICE_SERVICES_REQUEST, RULE_SERVICE_SERVICES_SUCCESS, RULE_SERVICE_SERVICES_FAILURE],
        endpoint: `rules/services/${ruleName}/services`,
        schema: Schemas.SERVICE_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_SERVICE_SERVICES = 'ADD_RULE_SERVICE_SERVICES';

export function addRuleServices(ruleName: string, services: string[]): EntitiesAction {
    return {
        type: ADD_RULE_SERVICE_SERVICES,
        entity: ruleName,
        data: {serviceNames: services}
    }
}

export const REMOVE_RULE_SERVICE_SERVICES = 'REMOVE_RULE_SERVICE_SERVICES';

export function removeRuleServices(ruleName: string, services: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_SERVICE_SERVICES,
        entity: ruleName,
        data: {serviceNames: services}
    }
}

export const RULES_CONTAINER_REQUEST = 'RULES_CONTAINER_REQUEST';
export const RULES_CONTAINER_SUCCESS = 'RULES_CONTAINER_SUCCESS';
export const RULES_CONTAINER_FAILURE = 'RULES_CONTAINER_FAILURE';
export const RULE_CONTAINER_REQUEST = 'RULE_CONTAINER_REQUEST';
export const RULE_CONTAINER_SUCCESS = 'RULE_CONTAINER_SUCCESS';
export const RULE_CONTAINER_FAILURE = 'RULE_CONTAINER_FAILURE';
export const loadRulesContainer = (name?: string) => (dispatch: any) => {
    return dispatch(fetchRulesContainer(name));
};
const fetchRulesContainer = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [RULES_CONTAINER_REQUEST, RULES_CONTAINER_SUCCESS, RULES_CONTAINER_FAILURE],
                endpoint: `rules/containers`,
                schema: Schemas.RULE_CONTAINER_ARRAY,
                entity: 'rules'
            }
            : {
                types: [RULE_CONTAINER_REQUEST, RULE_CONTAINER_SUCCESS, RULE_CONTAINER_FAILURE],
                endpoint: `rules/containers/${name}`,
                schema: Schemas.RULE_CONTAINER,
                entity: 'rules'
            }
});
export const ADD_RULE_CONTAINER = 'ADD_RULE_CONTAINER';

export function addRuleContainer(ruleContainer: IRuleContainer): EntitiesAction {
    return {
        type: ADD_RULE_CONTAINER,
        data: {containerRules: new Array(ruleContainer)}
    }
}

export const UPDATE_RULE_CONTAINER = 'UPDATE_RULE_CONTAINER';

export function updateRuleContainer(previousRuleContainer: IRuleContainer, currentRuleContainer: IRuleContainer): EntitiesAction {
    return {
        type: UPDATE_RULE_CONTAINER,
        data: {containerRules: [previousRuleContainer, currentRuleContainer]}
    }
}

export const DELETE_RULE_CONTAINER = 'DELETE_RULE_CONTAINER';

export function deleteContainerRule(containerRule: IRuleContainer): EntitiesAction {
    return {
        type: DELETE_RULE_CONTAINER,
        data: {containerRules: [containerRule]}
    }
}

export const RULE_CONTAINER_CONDITIONS_REQUEST = 'RULE_CONTAINER_CONDITIONS_REQUEST';
export const RULE_CONTAINER_CONDITIONS_SUCCESS = 'RULE_CONTAINER_CONDITIONS_SUCCESS';
export const RULE_CONTAINER_CONDITIONS_FAILURE = 'RULE_CONTAINER_CONDITIONS_FAILURE';
export const loadRuleContainerConditions = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleContainerConditions(ruleName));
};
const fetchRuleContainerConditions = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_CONTAINER_CONDITIONS_REQUEST, RULE_CONTAINER_CONDITIONS_SUCCESS, RULE_CONTAINER_CONDITIONS_FAILURE],
        endpoint: `rules/containers/${ruleName}/conditions`,
        schema: Schemas.RULE_CONDITION_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_CONTAINER_CONDITIONS = 'ADD_RULE_CONTAINER_CONDITIONS';

export function addRuleContainerConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: ADD_RULE_CONTAINER_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const REMOVE_RULE_CONTAINER_CONDITIONS = 'REMOVE_RULE_CONTAINER_CONDITIONS';

export function removeRuleContainerConditions(ruleName: string, conditions: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_CONTAINER_CONDITIONS,
        entity: ruleName,
        data: {conditionsNames: conditions}
    }
}

export const RULE_CONTAINER_CONTAINERS_REQUEST = 'RULE_CONTAINER_CONTAINERS_REQUEST';
export const RULE_CONTAINER_CONTAINERS_SUCCESS = 'RULE_CONTAINER_CONTAINERS_SUCCESS';
export const RULE_CONTAINER_CONTAINERS_FAILURE = 'RULE_CONTAINER_CONTAINERS_FAILURE';
export const loadRuleContainers = (ruleName: string) => (dispatch: any) => {
    return dispatch(fetchRuleContainerContainers(ruleName));
};
const fetchRuleContainerContainers = (ruleName: string) => ({
    [CALL_API]: {
        types: [RULE_CONTAINER_CONTAINERS_REQUEST, RULE_CONTAINER_CONTAINERS_SUCCESS, RULE_CONTAINER_CONTAINERS_FAILURE],
        endpoint: `rules/containers/${ruleName}/containers`,
        schema: Schemas.CONTAINER_ARRAY,
        entity: ruleName
    }
});
export const ADD_RULE_CONTAINER_CONTAINERS = 'ADD_RULE_CONTAINER_CONTAINERS';

export function addRuleContainers(ruleName: string, containers: string[]): EntitiesAction {
    return {
        type: ADD_RULE_CONTAINER_CONTAINERS,
        entity: ruleName,
        data: {containerIds: containers}
    }
}

export const REMOVE_RULE_CONTAINER_CONTAINERS = 'REMOVE_RULE_CONTAINER_CONTAINERS';

export function removeRuleContainers(ruleName: string, containers: string[]): EntitiesAction {
    return {
        type: REMOVE_RULE_CONTAINER_CONTAINERS,
        entity: ruleName,
        data: {containerIds: containers}
    }
}

export const CONDITIONS_REQUEST = 'CONDITIONS_REQUEST';
export const CONDITIONS_SUCCESS = 'CONDITIONS_SUCCESS';
export const CONDITIONS_FAILURE = 'CONDITIONS_FAILURE';
export const CONDITION_REQUEST = 'CONDITION_REQUEST';
export const CONDITION_SUCCESS = 'CONDITION_SUCCESS';
export const CONDITION_FAILURE = 'CONDITION_FAILURE';
export const loadConditions = (name?: string) => (dispatch: any) => {
    return dispatch(fetchConditions(name));
};
const fetchConditions = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [CONDITIONS_REQUEST, CONDITIONS_SUCCESS, CONDITIONS_FAILURE],
                endpoint: `rules/conditions`,
                schema: Schemas.RULE_CONDITION_ARRAY,
                entity: 'conditions'
            }
            : {
                types: [CONDITION_REQUEST, CONDITION_SUCCESS, CONDITION_FAILURE],
                endpoint: `rules/conditions/${name}`,
                schema: Schemas.RULE_CONDITION,
                entity: 'conditions'
            }
});
export const ADD_CONDITION = 'ADD_CONDITION';

export function addCondition(condition: IRuleCondition): EntitiesAction {
    return {
        type: ADD_CONDITION,
        data: {conditions: new Array(condition)}
    }
}

export const UPDATE_CONDITION = 'UPDATE_CONDITION';

export function updateCondition(previousCondition: IRuleCondition, currentCondition: IRuleCondition): EntitiesAction {
    return {
        type: UPDATE_CONDITION,
        data: {conditions: [previousCondition, currentCondition]}
    }
}

export const DELETE_CONDITION = 'DELETE_CONDITION';

export function deleteCondition(condition: IRuleCondition): EntitiesAction {
    return {
        type: DELETE_CONDITION,
        data: {conditions: [condition]}
    }
}

export const VALUE_MODES_REQUEST = 'VALUE_MODES_REQUEST';
export const VALUE_MODES_SUCCESS = 'VALUE_MODES_SUCCESS';
export const VALUE_MODES_FAILURE = 'VALUE_MODES_FAILURE';
export const loadValueModes = () => (dispatch: any) => {
    return dispatch(fetchValueModes());
};
const fetchValueModes = () => ({
    [CALL_API]: {
        types: [VALUE_MODES_REQUEST, VALUE_MODES_SUCCESS, VALUE_MODES_FAILURE],
        endpoint: `value-modes`,
        schema: Schemas.VALUE_MODE_ARRAY,
        entity: 'value-modes'
    }
});

export const FIELDS_REQUEST = 'FIELDS_REQUEST';
export const FIELDS_SUCCESS = 'FIELDS_SUCCESS';
export const FIELDS_FAILURE = 'FIELDS_FAILURE';
export const loadFields = () => (dispatch: any) => {
    return dispatch(fetchFields());
};
const fetchFields = () => ({
    [CALL_API]: {
        types: [FIELDS_REQUEST, FIELDS_SUCCESS, FIELDS_FAILURE],
        endpoint: `fields`,
        schema: Schemas.FIELD_ARRAY,
        entity: 'fields'
    }
});

export const OPERATORS_REQUEST = 'OPERATORS_REQUEST';
export const OPERATORS_SUCCESS = 'OPERATORS_SUCCESS';
export const OPERATORS_FAILURE = 'OPERATORS_FAILURE';
export const loadOperators = () => (dispatch: any) => {
    return dispatch(fetchOperators());
};
const fetchOperators = () => ({
    [CALL_API]: {
        types: [OPERATORS_REQUEST, OPERATORS_SUCCESS, OPERATORS_FAILURE],
        endpoint: `operators`,
        schema: Schemas.OPERATOR_ARRAY,
        entity: 'operators'
    }
});

export const DECISIONS_REQUEST = 'DECISIONS_REQUEST';
export const DECISIONS_SUCCESS = 'DECISIONS_SUCCESS';
export const DECISIONS_FAILURE = 'DECISIONS_FAILURE';
export const DECISION_REQUEST = 'DECISION_REQUEST';
export const DECISION_SUCCESS = 'DECISION_SUCCESS';
export const DECISION_FAILURE = 'DECISION_FAILURE';
export const loadDecisions = (name?: string) => (dispatch: any) => {
    return dispatch(fetchDecisions(name));
};
const fetchDecisions = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [DECISIONS_REQUEST, DECISIONS_SUCCESS, DECISIONS_FAILURE],
                endpoint: `decisions`,
                schema: Schemas.DECISION_ARRAY,
                entity: 'decisions'
            }
            : {
                types: [DECISION_REQUEST, DECISION_SUCCESS, DECISION_FAILURE],
                endpoint: `decisions`,
                schema: Schemas.DECISION,
                entity: 'decisions'
            }
});

export const SIMULATED_HOST_METRICS_REQUEST = 'SIMULATED_HOST_METRICS_REQUEST';
export const SIMULATED_HOST_METRIC_REQUEST = 'SIMULATED_HOST_METRIC_REQUEST';
export const SIMULATED_HOST_METRICS_SUCCESS = 'SIMULATED_HOST_METRICS_SUCCESS';
export const SIMULATED_HOST_METRIC_SUCCESS = 'SIMULATED_HOST_METRIC_SUCCESS';
export const SIMULATED_HOST_METRICS_FAILURE = 'SIMULATED_HOST_METRICS_FAILURE';
export const SIMULATED_HOST_METRIC_FAILURE = 'SIMULATED_HOST_METRIC_FAILURE';
export const loadSimulatedHostMetrics = (name?: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedHostMetrics(name));
};
const fetchSimulatedHostMetrics = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [SIMULATED_HOST_METRICS_REQUEST, SIMULATED_HOST_METRICS_SUCCESS, SIMULATED_HOST_METRICS_FAILURE],
                endpoint: `simulated-metrics/hosts`,
                schema: Schemas.SIMULATED_HOST_METRIC_ARRAY,
                entity: 'simulatedHostMetrics'
            }
            : {
                types: [SIMULATED_HOST_METRIC_REQUEST, SIMULATED_HOST_METRIC_SUCCESS, SIMULATED_HOST_METRIC_FAILURE],
                endpoint: `simulated-metrics/hosts/${name}`,
                schema: Schemas.SIMULATED_HOST_METRIC,
                entity: 'simulatedHostMetrics'
            }
});
export const ADD_SIMULATED_HOST_METRIC = 'ADD_SIMULATED_HOST_METRIC';

export function addSimulatedHostMetric(simulatedHostMetric: ISimulatedHostMetric): EntitiesAction {
    return {
        type: ADD_SIMULATED_HOST_METRIC,
        data: {simulatedHostMetrics: new Array(simulatedHostMetric)}
    }
}

export const UPDATE_SIMULATED_HOST_METRIC = 'UPDATE_SIMULATED_HOST_METRIC';

export function updateSimulatedHostMetric(previousSimulatedHostMetric: ISimulatedHostMetric,
                                          currentSimulatedHostMetric: ISimulatedHostMetric): EntitiesAction {
    return {
        type: UPDATE_SIMULATED_HOST_METRIC,
        data: {simulatedHostMetrics: [previousSimulatedHostMetric, currentSimulatedHostMetric]}
    }
}

export const DELETE_SIMULATED_HOST_METRIC = 'DELETE_SIMULATED_HOST_METRIC';

export function deleteSimulatedHostMetric(simulatedHostMetric: ISimulatedHostMetric): EntitiesAction {
    return {
        type: DELETE_SIMULATED_HOST_METRIC,
        data: {simulatedHostMetrics: [simulatedHostMetric]}
    }
}

export const SIMULATED_HOST_METRIC_CLOUD_HOSTS_REQUEST = 'SIMULATED_HOST_METRIC_CLOUD_HOSTS_REQUEST';
export const SIMULATED_HOST_METRIC_CLOUD_HOSTS_SUCCESS = 'SIMULATED_HOST_METRIC_CLOUD_HOSTS_SUCCESS';
export const SIMULATED_HOST_METRIC_CLOUD_HOSTS_FAILURE = 'SIMULATED_HOST_METRIC_CLOUD_HOSTS_FAILURE';
export const loadSimulatedHostMetricCloudHosts = (simulatedHostMetricName: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedHostMetricCloudHosts(simulatedHostMetricName));
};
const fetchSimulatedHostMetricCloudHosts = (simulatedHostMetricName: string) => ({
    [CALL_API]: {
        types: [SIMULATED_HOST_METRIC_CLOUD_HOSTS_REQUEST, SIMULATED_HOST_METRIC_CLOUD_HOSTS_SUCCESS, SIMULATED_HOST_METRIC_CLOUD_HOSTS_FAILURE],
        endpoint: `simulated-metrics/hosts/${simulatedHostMetricName}/cloud-hosts`,
        schema: Schemas.CLOUD_HOST_ARRAY,
        entity: simulatedHostMetricName
    }
});
export const ADD_SIMULATED_HOST_METRIC_CLOUD_HOSTS = 'ADD_SIMULATED_HOST_METRIC_CLOUD_HOSTS';

export function addSimulatedHostMetricCloudHosts(simulatedHostMetricName: string, cloudHosts: string[]): EntitiesAction {
    return {
        type: ADD_SIMULATED_HOST_METRIC_CLOUD_HOSTS,
        entity: simulatedHostMetricName,
        data: {cloudHostsId: cloudHosts}
    }
}

export const REMOVE_SIMULATED_HOST_METRIC_CLOUD_HOSTS = 'REMOVE_SIMULATED_HOST_METRIC_CLOUD_HOSTS';

export function removeSimulatedHostMetricCloudHosts(simulatedHostMetricName: string, cloudHosts: string[]): EntitiesAction {
    return {
        type: REMOVE_SIMULATED_HOST_METRIC_CLOUD_HOSTS,
        entity: simulatedHostMetricName,
        data: {cloudHostsId: cloudHosts}
    }
}

export const SIMULATED_HOST_METRIC_EDGE_HOSTS_REQUEST = 'SIMULATED_HOST_METRIC_EDGE_HOSTS_REQUEST';
export const SIMULATED_HOST_METRIC_EDGE_HOSTS_SUCCESS = 'SIMULATED_HOST_METRIC_EDGE_HOSTS_SUCCESS';
export const SIMULATED_HOST_METRIC_EDGE_HOSTS_FAILURE = 'SIMULATED_HOST_METRIC_EDGE_HOSTS_FAILURE';
export const loadSimulatedHostMetricEdgeHosts = (simulatedHostMetricName: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedHostMetricEdgeHosts(simulatedHostMetricName));
};
const fetchSimulatedHostMetricEdgeHosts = (simulatedHostMetricName: string) => ({
    [CALL_API]: {
        types: [SIMULATED_HOST_METRIC_EDGE_HOSTS_REQUEST, SIMULATED_HOST_METRIC_EDGE_HOSTS_SUCCESS, SIMULATED_HOST_METRIC_EDGE_HOSTS_FAILURE],
        endpoint: `simulated-metrics/hosts/${simulatedHostMetricName}/edge-hosts`,
        schema: Schemas.EDGE_HOST_ARRAY,
        entity: simulatedHostMetricName
    }
});
export const ADD_SIMULATED_HOST_METRIC_EDGE_HOSTS = 'ADD_SIMULATED_HOST_METRIC_EDGE_HOSTS';

export function addSimulatedHostMetricEdgeHosts(simulatedHostMetricName: string, edgeHosts: string[]): EntitiesAction {
    return {
        type: ADD_SIMULATED_HOST_METRIC_EDGE_HOSTS,
        entity: simulatedHostMetricName,
        data: {edgeHostsHostname: edgeHosts}
    }
}

export const REMOVE_SIMULATED_HOST_METRIC_EDGE_HOSTS = 'REMOVE_SIMULATED_HOST_METRIC_EDGE_HOSTS';

export function removeSimulatedHostMetricEdgeHosts(simulatedHostMetricName: string, edgeHosts: string[]): EntitiesAction {
    return {
        type: REMOVE_SIMULATED_HOST_METRIC_EDGE_HOSTS,
        entity: simulatedHostMetricName,
        data: {edgeHostsHostname: edgeHosts}
    }
}

export const SIMULATED_APP_METRICS_REQUEST = 'SIMULATED_APP_METRICS_REQUEST';
export const SIMULATED_APP_METRIC_REQUEST = 'SIMULATED_APP_METRIC_REQUEST';
export const SIMULATED_APP_METRICS_SUCCESS = 'SIMULATED_APP_METRICS_SUCCESS';
export const SIMULATED_APP_METRIC_SUCCESS = 'SIMULATED_APP_METRIC_SUCCESS';
export const SIMULATED_APP_METRICS_FAILURE = 'SIMULATED_APP_METRICS_FAILURE';
export const SIMULATED_APP_METRIC_FAILURE = 'SIMULATED_APP_METRIC_FAILURE';
export const loadSimulatedAppMetrics = (name?: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedAppMetrics(name));
};
const fetchSimulatedAppMetrics = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [SIMULATED_APP_METRICS_REQUEST, SIMULATED_APP_METRICS_SUCCESS, SIMULATED_APP_METRICS_FAILURE],
                endpoint: `simulated-metrics/apps`,
                schema: Schemas.SIMULATED_APP_METRIC_ARRAY,
                entity: 'simulatedAppMetrics'
            }
            : {
                types: [SIMULATED_APP_METRIC_REQUEST, SIMULATED_APP_METRIC_SUCCESS, SIMULATED_APP_METRIC_FAILURE],
                endpoint: `simulated-metrics/apps/${name}`,
                schema: Schemas.SIMULATED_APP_METRIC,
                entity: 'simulatedAppMetrics'
            }
});
export const ADD_SIMULATED_APP_METRIC = 'ADD_SIMULATED_APP_METRIC';

export function addSimulatedAppMetric(simulatedAppMetric: ISimulatedAppMetric): EntitiesAction {
    return {
        type: ADD_SIMULATED_APP_METRIC,
        data: {simulatedAppMetrics: new Array(simulatedAppMetric)}
    }
}

export const UPDATE_SIMULATED_APP_METRIC = 'UPDATE_SIMULATED_APP_METRIC';

export function updateSimulatedAppMetric(previousSimulatedAppMetric: ISimulatedAppMetric,
                                         currentSimulatedAppMetric: ISimulatedAppMetric): EntitiesAction {
    return {
        type: UPDATE_SIMULATED_APP_METRIC,
        data: {simulatedAppMetrics: [previousSimulatedAppMetric, currentSimulatedAppMetric]}
    }
}

export const DELETE_SIMULATED_APP_METRIC = 'DELETE_SIMULATED_APP_METRIC';

export function deleteSimulatedAppMetric(simulatedAppMetric: ISimulatedAppMetric): EntitiesAction {
    return {
        type: DELETE_SIMULATED_APP_METRIC,
        data: {simulatedAppMetrics: [simulatedAppMetric]}
    }
}

export const SIMULATED_APP_METRIC_APPS_REQUEST = 'SIMULATED_APP_METRIC_APPS_REQUEST';
export const SIMULATED_APP_METRIC_APPS_SUCCESS = 'SIMULATED_APP_METRIC_APPS_SUCCESS';
export const SIMULATED_APP_METRIC_APPS_FAILURE = 'SIMULATED_APP_METRIC_APPS_FAILURE';
export const loadSimulatedAppMetricApps = (simulatedAppMetricName: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedAppMetricApps(simulatedAppMetricName));
};
const fetchSimulatedAppMetricApps = (simulatedAppMetricName: string) => ({
    [CALL_API]: {
        types: [SIMULATED_APP_METRIC_APPS_REQUEST, SIMULATED_APP_METRIC_APPS_SUCCESS, SIMULATED_APP_METRIC_APPS_FAILURE],
        endpoint: `simulated-metrics/apps/${simulatedAppMetricName}/apps`,
        schema: Schemas.APP_ARRAY,
        entity: simulatedAppMetricName
    }
});
export const ADD_SIMULATED_APP_METRIC_APPS = 'ADD_SIMULATED_APP_METRIC_APPS';

export function addSimulatedAppMetricApps(simulatedAppMetricName: string, apps: string[]): EntitiesAction {
    return {
        type: ADD_SIMULATED_APP_METRIC_APPS,
        entity: simulatedAppMetricName,
        data: {appsNames: apps}
    }
}

export const REMOVE_SIMULATED_APP_METRIC_APPS = 'REMOVE_SIMULATED_APP_METRIC_APPS';

export function removeSimulatedAppMetricApps(simulatedAppMetricName: string, apps: string[]): EntitiesAction {
    return {
        type: REMOVE_SIMULATED_APP_METRIC_APPS,
        entity: simulatedAppMetricName,
        data: {appsNames: apps}
    }
}

export const SIMULATED_SERVICE_METRICS_REQUEST = 'SIMULATED_SERVICE_METRICS_REQUEST';
export const SIMULATED_SERVICE_METRIC_REQUEST = 'SIMULATED_SERVICE_METRIC_REQUEST';
export const SIMULATED_SERVICE_METRICS_SUCCESS = 'SIMULATED_SERVICE_METRICS_SUCCESS';
export const SIMULATED_SERVICE_METRIC_SUCCESS = 'SIMULATED_SERVICE_METRIC_SUCCESS';
export const SIMULATED_SERVICE_METRICS_FAILURE = 'SIMULATED_SERVICE_METRICS_FAILURE';
export const SIMULATED_SERVICE_METRIC_FAILURE = 'SIMULATED_SERVICE_METRIC_FAILURE';
export const loadSimulatedServiceMetrics = (name?: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedServiceMetrics(name));
};
const fetchSimulatedServiceMetrics = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [SIMULATED_SERVICE_METRICS_REQUEST, SIMULATED_SERVICE_METRICS_SUCCESS, SIMULATED_SERVICE_METRICS_FAILURE],
                endpoint: `simulated-metrics/services`,
                schema: Schemas.SIMULATED_SERVICE_METRIC_ARRAY,
                entity: 'simulatedServiceMetrics'
            }
            : {
                types: [SIMULATED_SERVICE_METRIC_REQUEST, SIMULATED_SERVICE_METRIC_SUCCESS, SIMULATED_SERVICE_METRIC_FAILURE],
                endpoint: `simulated-metrics/services/${name}`,
                schema: Schemas.SIMULATED_SERVICE_METRIC,
                entity: 'simulatedServiceMetrics'
            }
});
export const ADD_SIMULATED_SERVICE_METRIC = 'ADD_SIMULATED_SERVICE_METRIC';

export function addSimulatedServiceMetric(simulatedServiceMetric: ISimulatedServiceMetric): EntitiesAction {
    return {
        type: ADD_SIMULATED_SERVICE_METRIC,
        data: {simulatedServiceMetrics: new Array(simulatedServiceMetric)}
    }
}

export const UPDATE_SIMULATED_SERVICE_METRIC = 'UPDATE_SIMULATED_SERVICE_METRIC';

export function updateSimulatedServiceMetric(previousSimulatedServiceMetric: ISimulatedServiceMetric,
                                             currentSimulatedServiceMetric: ISimulatedServiceMetric): EntitiesAction {
    return {
        type: UPDATE_SIMULATED_SERVICE_METRIC,
        data: {simulatedServiceMetrics: [previousSimulatedServiceMetric, currentSimulatedServiceMetric]}
    }
}

export const DELETE_SIMULATED_SERVICE_METRIC = 'DELETE_SIMULATED_SERVICE_METRIC';

export function deleteSimulatedServiceMetric(simulatedServiceMetric: ISimulatedServiceMetric): EntitiesAction {
    return {
        type: DELETE_SIMULATED_SERVICE_METRIC,
        data: {simulatedServiceMetrics: [simulatedServiceMetric]}
    }
}

export const SIMULATED_SERVICE_METRIC_SERVICES_REQUEST = 'SIMULATED_SERVICE_METRIC_SERVICES_REQUEST';
export const SIMULATED_SERVICE_METRIC_SERVICES_SUCCESS = 'SIMULATED_SERVICE_METRIC_SERVICES_SUCCESS';
export const SIMULATED_SERVICE_METRIC_SERVICES_FAILURE = 'SIMULATED_SERVICE_METRIC_SERVICES_FAILURE';
export const loadSimulatedServiceMetricServices = (simulatedServiceMetricName: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedServiceMetricServices(simulatedServiceMetricName));
};
const fetchSimulatedServiceMetricServices = (simulatedServiceMetricName: string) => ({
    [CALL_API]: {
        types: [SIMULATED_SERVICE_METRIC_SERVICES_REQUEST, SIMULATED_SERVICE_METRIC_SERVICES_SUCCESS, SIMULATED_SERVICE_METRIC_SERVICES_FAILURE],
        endpoint: `simulated-metrics/services/${simulatedServiceMetricName}/services`,
        schema: Schemas.SERVICE_ARRAY,
        entity: simulatedServiceMetricName
    }
});
export const ADD_SIMULATED_SERVICE_METRIC_SERVICES = 'ADD_SIMULATED_SERVICE_METRIC_SERVICES';

export function addSimulatedServiceMetricServices(simulatedServiceMetricName: string, services: string[]): EntitiesAction {
    return {
        type: ADD_SIMULATED_SERVICE_METRIC_SERVICES,
        entity: simulatedServiceMetricName,
        data: {serviceNames: services}
    }
}

export const REMOVE_SIMULATED_SERVICE_METRIC_SERVICES = 'REMOVE_SIMULATED_SERVICE_METRIC_SERVICES';

export function removeSimulatedServiceMetricServices(simulatedServiceMetricName: string, services: string[]): EntitiesAction {
    return {
        type: REMOVE_SIMULATED_SERVICE_METRIC_SERVICES,
        entity: simulatedServiceMetricName,
        data: {serviceNames: services}
    }
}

export const SIMULATED_CONTAINER_METRICS_REQUEST = 'SIMULATED_CONTAINER_METRICS_REQUEST';
export const SIMULATED_CONTAINER_METRIC_REQUEST = 'SIMULATED_CONTAINER_METRIC_REQUEST';
export const SIMULATED_CONTAINER_METRICS_SUCCESS = 'SIMULATED_CONTAINER_METRICS_SUCCESS';
export const SIMULATED_CONTAINER_METRIC_SUCCESS = 'SIMULATED_CONTAINER_METRIC_SUCCESS';
export const SIMULATED_CONTAINER_METRICS_FAILURE = 'SIMULATED_CONTAINER_METRICS_FAILURE';
export const SIMULATED_CONTAINER_METRIC_FAILURE = 'SIMULATED_CONTAINER_METRIC_FAILURE';
export const loadSimulatedContainerMetrics = (name?: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedContainerMetrics(name));
};
const fetchSimulatedContainerMetrics = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [SIMULATED_CONTAINER_METRICS_REQUEST, SIMULATED_CONTAINER_METRICS_SUCCESS, SIMULATED_CONTAINER_METRICS_FAILURE],
                endpoint: `simulated-metrics/containers`,
                schema: Schemas.SIMULATED_CONTAINER_METRIC_ARRAY,
                entity: 'simulatedContainerMetrics'
            }
            : {
                types: [SIMULATED_CONTAINER_METRIC_REQUEST, SIMULATED_CONTAINER_METRIC_SUCCESS, SIMULATED_CONTAINER_METRIC_FAILURE],
                endpoint: `simulated-metrics/containers/${name}`,
                schema: Schemas.SIMULATED_CONTAINER_METRIC,
                entity: 'simulatedContainerMetrics'
            }
});
export const ADD_SIMULATED_CONTAINER_METRIC = 'ADD_SIMULATED_CONTAINER_METRIC';

export function addSimulatedContainerMetric(simulatedContainerMetric: ISimulatedContainerMetric): EntitiesAction {
    return {
        type: ADD_SIMULATED_CONTAINER_METRIC,
        data: {simulatedContainerMetrics: new Array(simulatedContainerMetric)}
    }
}

export const UPDATE_SIMULATED_CONTAINER_METRIC = 'UPDATE_SIMULATED_CONTAINER_METRIC';

export function updateSimulatedContainerMetric(previousSimulatedContainerMetric: ISimulatedContainerMetric,
                                               currentSimulatedContainerMetric: ISimulatedContainerMetric): EntitiesAction {
    return {
        type: UPDATE_SIMULATED_CONTAINER_METRIC,
        data: {simulatedContainerMetrics: [previousSimulatedContainerMetric, currentSimulatedContainerMetric]}
    }
}

export const DELETE_SIMULATED_CONTAINER_METRIC = 'DELETE_SIMULATED_CONTAINER_METRIC';

export function deleteSimulatedContainerMetric(simulatedContainerMetric: ISimulatedContainerMetric): EntitiesAction {
    return {
        type: DELETE_SIMULATED_CONTAINER_METRIC,
        data: {simulatedContainerMetrics: [simulatedContainerMetric]}
    }
}

export const SIMULATED_CONTAINER_METRIC_CONTAINERS_REQUEST = 'SIMULATED_CONTAINER_METRIC_CONTAINERS_REQUEST';
export const SIMULATED_CONTAINER_METRIC_CONTAINERS_SUCCESS = 'SIMULATED_CONTAINER_METRIC_CONTAINERS_SUCCESS';
export const SIMULATED_CONTAINER_METRIC_CONTAINERS_FAILURE = 'SIMULATED_CONTAINER_METRIC_CONTAINERS_FAILURE';
export const loadSimulatedContainerMetricContainers = (simulatedContainerMetricName: string) => (dispatch: any) => {
    return dispatch(fetchSimulatedContainerMetricContainers(simulatedContainerMetricName));
};
const fetchSimulatedContainerMetricContainers = (simulatedContainerMetricName: string) => ({
    [CALL_API]: {
        types: [SIMULATED_CONTAINER_METRIC_CONTAINERS_REQUEST, SIMULATED_CONTAINER_METRIC_CONTAINERS_SUCCESS, SIMULATED_CONTAINER_METRIC_CONTAINERS_FAILURE],
        endpoint: `simulated-metrics/containers/${simulatedContainerMetricName}/containers`,
        schema: Schemas.CONTAINER_ARRAY,
        entity: simulatedContainerMetricName
    }
});
export const ADD_SIMULATED_CONTAINER_METRIC_CONTAINERS = 'ADD_SIMULATED_CONTAINER_METRIC_CONTAINERS';

export function addSimulatedContainerMetricContainers(simulatedContainerMetricName: string, containers: string[]): EntitiesAction {
    return {
        type: ADD_SIMULATED_CONTAINER_METRIC_CONTAINERS,
        entity: simulatedContainerMetricName,
        data: {containerIds: containers}
    }
}

export const REMOVE_SIMULATED_CONTAINER_METRIC_CONTAINERS = 'REMOVE_SIMULATED_CONTAINER_METRIC_CONTAINERS';

export function removeSimulatedContainerMetricContainers(simulatedContainerMetricName: string, containers: string[]): EntitiesAction {
    return {
        type: REMOVE_SIMULATED_CONTAINER_METRIC_CONTAINERS,
        entity: simulatedContainerMetricName,
        data: {containerIds: containers}
    }
}

export const REGIONS_REQUEST = 'REGIONS_REQUEST';
export const REGIONS_SUCCESS = 'REGIONS_SUCCESS';
export const REGIONS_FAILURE = 'REGIONS_FAILURE';
export const REGION_REQUEST = 'REGION_REQUEST';
export const REGION_SUCCESS = 'REGION_SUCCESS';
export const REGION_FAILURE = 'REGION_FAILURE';
export const loadRegions = (name?: string) => (dispatch: any) => {
    return dispatch(fetchRegions(name));
};
const fetchRegions = (name?: string) => ({
    [CALL_API]:
        !name
            ? {
                types: [REGIONS_REQUEST, REGIONS_SUCCESS, REGIONS_FAILURE],
                endpoint: `regions`,
                schema: Schemas.REGION_ARRAY,
                entity: 'regions'
            }
            : {
                types: [REGION_REQUEST, REGION_SUCCESS, REGION_FAILURE],
                endpoint: `regions/${name}`,
                schema: Schemas.REGION,
                entity: 'regions'
            }
});

/*export const ADD_REGION = 'ADD_REGION';
export function addRegion(awsRegion: IRegion): EntitiesAction {
    return {
        type: ADD_REGION,
        data: {regions: new Array(awsRegion)}
    }
}*/

/*export const UPDATE_REGION = 'UPDATE_REGION';
export function updateRegion(previousRegion: IRegion, currentRegion: IRegion): EntitiesAction {
    return {
        type: UPDATE_REGION,
        data: {regions: [previousRegion, currentRegion]}
    }
}*/

/*export const DELETE_REGION = 'DELETE_REGION';
export function deleteRegion(awsRegion: IRegion): EntitiesAction {
    return {
        type: DELETE_REGION,
        data: {regions: [awsRegion]}
    }
}*/

export const LOAD_BALANCERS_REQUEST = 'LOAD_BALANCERS_REQUEST';
export const LOAD_BALANCER_REQUEST = 'LOAD_BALANCER_REQUEST';
export const LOAD_BALANCERS_SUCCESS = 'LOAD_BALANCERS_SUCCESS';
export const LOAD_BALANCER_SUCCESS = 'LOAD_BALANCER_SUCCESS';
export const LOAD_BALANCERS_FAILURE = 'LOAD_BALANCERS_FAILURE';
export const LOAD_BALANCER_FAILURE = 'LOAD_BALANCER_FAILURE';
export const loadLoadBalancers = () => (dispatch: any) => {
    return dispatch(fetchLoadBalancers());
};
const fetchLoadBalancers = (id?: string) => ({
    [CALL_API]:
        !id
            ? {
                types: [LOAD_BALANCERS_REQUEST, LOAD_BALANCERS_SUCCESS, LOAD_BALANCERS_FAILURE],
                endpoint: `containers?serviceName=load-balancer`,
                schema: Schemas.LOAD_BALANCER_ARRAY,
                entity: 'loadBalancers'
            }
            : {
                types: [LOAD_BALANCER_REQUEST, LOAD_BALANCER_SUCCESS, LOAD_BALANCER_FAILURE],
                endpoint: `containers/${id}`,
                schema: Schemas.LOAD_BALANCER,
                entity: 'loadBalancers'
            }
});
export const ADD_LOAD_BALANCER = 'ADD_LOAD_BALANCER';

export function addLoadBalancer(loadBalancer: ILoadBalancer): EntitiesAction {
    return {
        type: ADD_LOAD_BALANCER,
        data: {loadBalancers: new Array(loadBalancer)}
    }
}

export const REGISTRATION_SERVERS_REQUEST = 'REGISTRATION_SERVERS_REQUEST';
export const REGISTRATION_SERVER_REQUEST = 'REGISTRATION_SERVER_REQUEST';
export const REGISTRATION_SERVERS_SUCCESS = 'REGISTRATION_SERVERS_SUCCESS';
export const REGISTRATION_SERVER_SUCCESS = 'REGISTRATION_SERVER_SUCCESS';
export const REGISTRATION_SERVERS_FAILURE = 'REGISTRATION_SERVERS_FAILURE';
export const REGISTRATION_SERVER_FAILURE = 'REGISTRATION_SERVER_FAILURE';
export const loadRegistrationServers = () => (dispatch: any) => {
    return dispatch(fetchRegistrationServers());
};
const fetchRegistrationServers = (id?: string) => ({
    [CALL_API]:
        !id
            ? {
                types: [REGISTRATION_SERVERS_REQUEST, REGISTRATION_SERVERS_SUCCESS, REGISTRATION_SERVERS_FAILURE],
                endpoint: `containers?serviceName=registration-server`,
                schema: Schemas.REGISTRATION_SERVER_ARRAY,
                entity: 'registrationServers'
            }
            : {
                types: [REGISTRATION_SERVER_REQUEST, REGISTRATION_SERVER_SUCCESS, REGISTRATION_SERVER_FAILURE],
                endpoint: `containers/${id}`,
                schema: Schemas.REGISTRATION_SERVER,
                entity: 'registrationServers'
            }
});
export const ADD_REGISTRATION_SERVER = 'ADD_REGISTRATION_SERVER';

export function addRegistrationServer(registrationServer: IRegistrationServer): EntitiesAction {
    return {
        type: ADD_REGISTRATION_SERVER,
        data: {registrationServers: new Array(registrationServer)}
    }
}

export const WORKER_MANAGERS_REQUEST = 'WORKER_MANAGERS_REQUEST';
export const WORKER_MANAGER_REQUEST = 'WORKER_MANAGER_REQUEST';
export const WORKER_MANAGERS_SUCCESS = 'WORKER_MANAGERS_SUCCESS';
export const WORKER_MANAGER_SUCCESS = 'WORKER_MANAGER_SUCCESS';
export const WORKER_MANAGERS_FAILURE = 'WORKER_MANAGERS_FAILURE';
export const WORKER_MANAGER_FAILURE = 'WORKER_MANAGER_FAILURE';
export const loadWorkerManagers = () => (dispatch: any) => {
    return dispatch(fetchWorkerManagers());
};
const fetchWorkerManagers = (id?: string) => ({
    [CALL_API]:
        !id
            ? {
                types: [WORKER_MANAGERS_REQUEST, WORKER_MANAGERS_SUCCESS, WORKER_MANAGERS_FAILURE],
                endpoint: `worker-managers`,
                schema: Schemas.WORKER_MANAGER_ARRAY,
                entity: 'workerManagers'
            }
            : {
                types: [WORKER_MANAGER_REQUEST, WORKER_MANAGER_SUCCESS, WORKER_MANAGER_FAILURE],
                endpoint: `worker-managers/${id}`,
                schema: Schemas.WORKER_MANAGER,
                entity: 'workerManagers'
            }
});
export const ADD_WORKER_MANAGER = 'ADD_WORKER_MANAGER';

export function addWorkerManager(workerManager: IWorkerManager): EntitiesAction {
    return {
        type: ADD_WORKER_MANAGER,
        data: {workerManagers: new Array(workerManager)}
    }
}

export const DELETE_WORKER_MANAGER = 'DELETE_WORKER_MANAGER';

export function deleteWorkerManager(workerManager: IWorkerManager): EntitiesAction {
    return {
        type: DELETE_WORKER_MANAGER,
        data: {workerManagers: [workerManager]}
    }
}

export const WORKER_MANAGER_HOSTS_REQUEST = 'WORKER_MANAGER_HOSTS_REQUEST';
export const WORKER_MANAGER_HOSTS_SUCCESS = 'WORKER_MANAGER_HOSTS_SUCCESS';
export const WORKER_MANAGER_HOSTS_FAILURE = 'WORKER_MANAGER_HOSTS_FAILURE';
export const loadWorkerManagerHosts = (id: string) => (dispatch: any) => {
    return dispatch(fetchWorkerManagerHosts(id));
};
const fetchWorkerManagerHosts = (id: string) => ({
    [CALL_API]: {
        types: [WORKER_MANAGER_HOSTS_REQUEST, WORKER_MANAGER_HOSTS_SUCCESS, WORKER_MANAGER_HOSTS_FAILURE],
        endpoint: `worker-managers/${id}/assigned-hosts`,
        entity: id
    }
});

export const ASSIGN_WORKER_MANAGER_MACHINES = 'ASSIGN_WORKER_MANAGER_MACHINES';

export function assignWorkerManagerHosts(id: string, hosts: string[]): EntitiesAction {
    return {
        type: ASSIGN_WORKER_MANAGER_MACHINES,
        entity: id,
        data: {assignedHosts: hosts}
    }
}

export const UNASSIGN_WORKER_MANAGER_MACHINES = 'UNASSIGN_WORKER_MANAGER_MACHINES';

export function unassignWorkerManagerHosts(id: string, hosts: string[]): EntitiesAction {
    return {
        type: UNASSIGN_WORKER_MANAGER_MACHINES,
        entity: id,
        data: {assignedHosts: hosts}
    }
}

export const LOGS_REQUEST = 'LOGS_REQUEST';
export const LOGS_SUCCESS = 'LOGS_SUCCESS';
export const LOGS_FAILURE = 'LOGS_FAILURE';
export const loadLogs = () => (dispatch: any) => {
    return dispatch(fetchLogs());
};
const fetchLogs = () => ({
    [CALL_API]: {
        types: [LOGS_REQUEST, LOGS_SUCCESS, LOGS_FAILURE],
        endpoint: `logs`,
        schema: Schemas.LOGS_ARRAY,
    }
});

export const SCRIPTS_REQUEST = 'SCRIPTS_REQUEST';
export const SCRIPTS_SUCCESS = 'SCRIPTS_SUCCESS';
export const SCRIPTS_FAILURE = 'SCRIPTS_FAILURE';
export const loadScripts = () => (dispatch: any) => {
    return dispatch(fetchScripts());
};
const fetchScripts = () => ({
    [CALL_API]: {
        types: [SCRIPTS_REQUEST, SCRIPTS_SUCCESS, SCRIPTS_FAILURE],
        endpoint: `ssh/scripts`
    }
});

export const ADD_COMMANDS = 'ADD_COMMANDS';
export const addCommand = (command: ICommand | IFileTransfer) => (
    {
        type: ADD_COMMANDS,
        data: { commands: [command] }
    }
);

export const CLEAR_COMMANDS = 'CLEAR_COMMANDS';
export const clearCommands = () => (
    {
        type: CLEAR_COMMANDS
    }
);

export const SIDENAV_SHOW_USER = 'SIDENAV_SHOW_USER';
export const showSidenavByUser = (value: boolean) => (
    {
        type: SIDENAV_SHOW_USER,
        value
    }
);

export const SIDENAV_SHOW_WIDTH = 'SIDENAV_SHOW_WIDTH';
export const showSidenavByWidth = (value: boolean) => (
    {
        type: SIDENAV_SHOW_WIDTH,
        value
    }
);

export const SEARCH_UPDATE = 'SEARCH_UPDATE';
export const updateSearch = (search: string) => (
    {
        type: SEARCH_UPDATE,
        search
    }
);

export const CHANGE_COMPONENT = 'CHANGE_COMPONENT';
export const changeComponent = (component: IComponent) => (
    {
        type: CHANGE_COMPONENT,
        component
    }
);

export const HIDE_CONFIRMATION_DIALOG = 'HIDE_CONFIRMATION_DIALOG';
export const hideConfirmationDialog = (hidden: boolean) => (
    {
        type: HIDE_CONFIRMATION_DIALOG,
        hidden
    }
);
