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

import {normalize, schema} from 'normalizr';
import {camelizeKeys} from 'humps';
import {IService} from "../routes/management/services/Service";
import {IServiceDependency} from "../routes/management/services/ServiceDependencyList";
import axios, {Method} from "axios";
import {API_URL, REQUEST_TIMEOUT} from "../utils/api";
import {ILogs} from "../routes/management/logs/ManagementLogs";
import {IRegion} from "../routes/management/regions/Region";
import {IDependent} from "../routes/management/services/ServiceDependentList";
import {IPrediction} from "../routes/management/services/ServicePredictionList";
import {INode} from "../routes/management/nodes/Node";
import {ICloudHost} from "../routes/management/hosts/cloud/CloudHost";
import {IEdgeHost} from "../routes/management/hosts/edge/EdgeHost";
import {IContainer} from "../routes/management/containers/Container";
import {IApp} from "../routes/management/apps/App";
import {IDecision, IField, IOperator, IValueMode} from "../routes/management/rules/Rule";
import {IRuleService} from "../routes/management/rules/services/RuleService";
import {IRuleHost} from "../routes/management/rules/hosts/RuleHost";
import {IRuleCondition} from "../routes/management/rules/conditions/RuleCondition";
import {IAppService} from "../routes/management/apps/AppServicesList";
import {ILoadBalancer} from "../routes/management/loadBalancers/LoadBalancer";
import {IRegistrationServer} from "../routes/management/registrationServers/RegistrationServer";
import {ISimulatedHostMetric} from "../routes/management/metrics/hosts/SimulatedHostMetric";
import {ISimulatedServiceMetric} from "../routes/management/metrics/services/SimulatedServiceMetric";
import {IRuleContainer} from "../routes/management/rules/containers/RuleContainer";
import {ISimulatedContainerMetric} from "../routes/management/metrics/containers/SimulatedContainerMetric";
import {IWorkerManager} from "../routes/management/workerManagers/WorkerManager";
import {IRuleApp} from "../routes/management/rules/apps/RuleApp";
import {ISimulatedAppMetric} from "../routes/management/metrics/apps/SimulatedAppMetric";

const callApi = (endpoint: string, schema?: any, method?: Method) => {
    const url = endpoint.includes(API_URL) ? endpoint : `${API_URL}/${endpoint}`;
    return axios(url, {
        method: method || 'get',
        //TODO remove options
        headers: {
            'Authorization': 'Basic YWRtaW46YWRtaW4=',
            'Content-type': 'application/json;charset=UTF-8',
            'Accept': 'application/json;charset=UTF-8',
        },
        timeout: REQUEST_TIMEOUT
    }).then(response => {
        if (response.status === 200) {
            const camelizedJson = camelizeKeys(response.data);
            return schema ? normalize(camelizedJson, schema).entities : camelizedJson;
        } else {
            return Promise.reject(response);
        }
    }).catch(e => Promise.reject(e))
};

interface ISchemas {
    APP: schema.Entity<IApp>;
    APP_ARRAY: schema.Entity<IApp>[];
    APP_SERVICE: schema.Entity<IAppService>;
    APP_SERVICE_ARRAY: schema.Entity<IAppService>[];
    APP_RULE: schema.Entity<IRuleApp>;
    APP_RULE_ARRAY: schema.Entity<IRuleApp>[];
    APP_SIMULATED_METRIC: schema.Entity<ISimulatedAppMetric>;
    APP_SIMULATED_METRIC_ARRAY: schema.Entity<ISimulatedAppMetric>[];
    SERVICE: schema.Entity<IService>;
    SERVICE_ARRAY: schema.Entity<IService>[];
    SERVICE_APP: schema.Entity<IApp>;
    SERVICE_APP_ARRAY: schema.Entity<IApp>[];
    SERVICE_DEPENDENCY: schema.Entity<IServiceDependency>;
    SERVICE_DEPENDENCY_ARRAY: schema.Entity<IServiceDependency>[];
    SERVICE_DEPENDENT: schema.Entity<IDependent>;
    SERVICE_DEPENDENT_ARRAY: schema.Entity<IDependent>[];
    SERVICE_PREDICTION: schema.Entity<IPrediction>;
    SERVICE_PREDICTION_ARRAY: schema.Entity<IPrediction>[];
    SERVICE_RULE: schema.Entity<IRuleService>;
    SERVICE_RULE_ARRAY: schema.Entity<IRuleService>[];
    SERVICE_SIMULATED_METRIC: schema.Entity<ISimulatedServiceMetric>;
    SERVICE_SIMULATED_METRIC_ARRAY: schema.Entity<ISimulatedServiceMetric>[];
    CONTAINER: schema.Entity<IContainer>;
    CONTAINER_ARRAY: schema.Entity<IContainer>[];
    CONTAINER_RULE: schema.Entity<IRuleContainer>;
    CONTAINER_RULE_ARRAY: schema.Entity<IRuleService>[];
    CONTAINER_SIMULATED_METRIC: schema.Entity<ISimulatedContainerMetric>;
    CONTAINER_SIMULATED_METRIC_ARRAY: schema.Entity<ISimulatedContainerMetric>[];
    CLOUD_HOST: schema.Entity<ICloudHost>;
    CLOUD_HOST_ARRAY: schema.Entity<ICloudHost>[];
    CLOUD_HOST_RULE: schema.Entity<IRuleHost>;
    CLOUD_HOST_RULE_ARRAY: schema.Entity<IRuleHost>[];
    CLOUD_HOST_SIMULATED_METRIC: schema.Entity<ISimulatedHostMetric>;
    CLOUD_HOST_SIMULATED_METRIC_ARRAY: schema.Entity<ISimulatedHostMetric>[];
    EDGE_HOST: schema.Entity<IEdgeHost>;
    EDGE_HOST_ARRAY: schema.Entity<IEdgeHost>[];
    EDGE_HOST_RULE: schema.Entity<IRuleHost>;
    EDGE_HOST_RULE_ARRAY: schema.Entity<IRuleHost>[];
    EDGE_HOST_SIMULATED_METRIC: schema.Entity<ISimulatedHostMetric>;
    EDGE_HOST_SIMULATED_METRIC_ARRAY: schema.Entity<ISimulatedHostMetric>[];
    NODE: schema.Entity<INode>;
    NODE_ARRAY: schema.Entity<INode>[];
    REGION: schema.Entity<IRegion>;
    REGION_ARRAY: schema.Entity<IRegion>[];
    RULE_HOST: schema.Entity<IRuleHost>;
    RULE_HOST_ARRAY: schema.Entity<IRuleHost>[];
    RULE_APP: schema.Entity<IRuleApp>;
    RULE_APP_ARRAY: schema.Entity<IRuleApp>[];
    RULE_SERVICE: schema.Entity<IRuleService>;
    RULE_SERVICE_ARRAY: schema.Entity<IRuleService>[];
    RULE_CONTAINER: schema.Entity<IRuleContainer>;
    RULE_CONTAINER_ARRAY: schema.Entity<IRuleContainer>[];
    RULE_CONDITION: schema.Entity<IRuleCondition>;
    RULE_CONDITION_ARRAY: schema.Entity<IRuleCondition>[];
    VALUE_MODE_ARRAY: schema.Entity<IValueMode>[];
    FIELD_ARRAY: schema.Entity<IField>[];
    OPERATOR_ARRAY: schema.Entity<IOperator>[];
    DECISION: schema.Entity<IDecision>;
    DECISION_ARRAY: schema.Entity<IDecision>[];
    SIMULATED_HOST_METRIC: schema.Entity<ISimulatedHostMetric>;
    SIMULATED_HOST_METRIC_ARRAY: schema.Entity<ISimulatedHostMetric>[];
    SIMULATED_APP_METRIC: schema.Entity<ISimulatedAppMetric>;
    SIMULATED_APP_METRIC_ARRAY: schema.Entity<ISimulatedAppMetric>[];
    SIMULATED_SERVICE_METRIC: schema.Entity<ISimulatedServiceMetric>;
    SIMULATED_SERVICE_METRIC_ARRAY: schema.Entity<ISimulatedServiceMetric>[];
    SIMULATED_CONTAINER_METRIC: schema.Entity<ISimulatedContainerMetric>;
    SIMULATED_CONTAINER_METRIC_ARRAY: schema.Entity<ISimulatedContainerMetric>[];
    LOAD_BALANCER: schema.Entity<ILoadBalancer>;
    LOAD_BALANCER_ARRAY: schema.Entity<ILoadBalancer>[];
    REGISTRATION_SERVER: schema.Entity<IRegistrationServer>;
    REGISTRATION_SERVER_ARRAY: schema.Entity<IRegistrationServer>[];
    WORKER_MANAGER: schema.Entity<IWorkerManager>;
    WORKER_MANAGER_ARRAY: schema.Entity<IWorkerManager>[];
    LOGS_ARRAY: schema.Entity<ILogs>[];
}

const app: schema.Entity<IApp> = new schema.Entity('apps', undefined, {
    idAttribute: (app: IApp) => app.name
});
const apps = new schema.Array(app);

const appService: schema.Entity<IAppService> = new schema.Entity('services', undefined, {
    idAttribute: (service: IAppService) => service.service.serviceName
});
const appServices = new schema.Array(appService);

const service: schema.Entity<IService> = new schema.Entity('services', undefined, {
    idAttribute: (service: IService) => service.serviceName
});
const services = new schema.Array(service);

const dependency: schema.Entity<IServiceDependency> = new schema.Entity('dependencies', undefined, {
    idAttribute: (dependency: IServiceDependency) => dependency.serviceName
});
const dependencies = new schema.Array(dependency);

const dependent: schema.Entity<IDependent> = new schema.Entity('dependents', undefined, {
    idAttribute: (dependent: IDependent) => dependent.serviceName
});
const dependents = new schema.Array(dependent);

const prediction: schema.Entity<IPrediction> = new schema.Entity('predictions', undefined, {
    idAttribute: (prediction: IPrediction) => prediction.name
});

const container: schema.Entity<IContainer> = new schema.Entity('containers', undefined, {
    idAttribute: (container: IContainer) => container.containerId
});
const containers = new schema.Array(container);

const cloudHost: schema.Entity<ICloudHost> = new schema.Entity('cloudHosts', undefined, {
    idAttribute: (host: ICloudHost) => host.instanceId
});
const cloudHosts = new schema.Array(cloudHost);

const edgeHost: schema.Entity<IEdgeHost> = new schema.Entity('edgeHosts', undefined, {
    idAttribute: (host: IEdgeHost) => host.publicIpAddress
});
const edgeHosts = new schema.Array(edgeHost);

const node: schema.Entity<INode> = new schema.Entity('nodes', undefined, {
    idAttribute: (node: INode) => node.id.toString()
});

const region: schema.Entity<IRegion> = new schema.Entity('regions', undefined, {
    idAttribute: (region: IRegion) => region.region
});

const ruleHost: schema.Entity<IRuleHost> = new schema.Entity('hostRules', undefined, {
    idAttribute: (hostRule: IRuleHost) => hostRule.name
});
const hostRules = new schema.Array(ruleHost);

const ruleApp: schema.Entity<IRuleApp> = new schema.Entity('appRules', undefined, {
    idAttribute: (appRule: IRuleApp) => appRule.name
});
const appRules = new schema.Array(ruleApp);

const ruleService: schema.Entity<IRuleService> = new schema.Entity('serviceRules', undefined, {
    idAttribute: (serviceRule: IRuleService) => serviceRule.name
});
const serviceRules = new schema.Array(ruleService);

const ruleContainer: schema.Entity<IRuleContainer> = new schema.Entity('containerRules', undefined, {
    idAttribute: (containerRule: IRuleContainer) => containerRule.name
});
const containerRules = new schema.Array(ruleContainer);

const valueMode: schema.Entity<IValueMode> = new schema.Entity('valueModes', undefined, {
    idAttribute: (valueMode: IValueMode) => valueMode.name
});
const valueModes = new schema.Array(valueMode);

const field: schema.Entity<IField> = new schema.Entity('fields', undefined, {
    idAttribute: (field: IField) => field.name
});
const fields = new schema.Array(field);

const operator: schema.Entity<IOperator> = new schema.Entity('operators', undefined, {
    idAttribute: (operator: IOperator) => operator.operator
});
const operators = new schema.Array(operator);

const condition: schema.Entity<IRuleCondition> = new schema.Entity('conditions', undefined, {
    idAttribute: (condition: IRuleCondition) => condition.name
});
const conditions = new schema.Array(condition);

const decision: schema.Entity<IDecision> = new schema.Entity('decisions', undefined, {
    idAttribute: (decision: IDecision) => decision.ruleDecision + "_" + decision.componentType.type.toUpperCase()
});

const simulatedHostMetric: schema.Entity<ISimulatedHostMetric> = new schema.Entity('simulatedHostMetrics', undefined, {
    idAttribute: (simulatedHostMetric: ISimulatedHostMetric) => simulatedHostMetric.name
});
const hostSimulatedMetrics = new schema.Array(simulatedHostMetric);

const simulatedAppMetric: schema.Entity<ISimulatedAppMetric> = new schema.Entity('simulatedAppMetrics', undefined, {
    idAttribute: (simulatedAppMetric: ISimulatedAppMetric) => simulatedAppMetric.name
});
const appSimulatedMetrics = new schema.Array(simulatedAppMetric);

const simulatedServiceMetric: schema.Entity<ISimulatedServiceMetric> = new schema.Entity('simulatedServiceMetrics', undefined, {
    idAttribute: (simulatedServiceMetric: ISimulatedServiceMetric) => simulatedServiceMetric.name
});
const serviceSimulatedMetrics = new schema.Array(simulatedServiceMetric);

const simulatedContainerMetric: schema.Entity<ISimulatedContainerMetric> = new schema.Entity('simulatedContainerMetrics', undefined, {
    idAttribute: (simulatedContainerMetric: ISimulatedContainerMetric) => simulatedContainerMetric.name
});
const containerSimulatedMetrics = new schema.Array(simulatedContainerMetric);

const loadBalancer: schema.Entity<ILoadBalancer> = new schema.Entity('loadBalancers', undefined, {
    idAttribute: (loadBalancer: ILoadBalancer) => loadBalancer.containerId
});

const registrationServer: schema.Entity<IRegistrationServer> = new schema.Entity('registrationServers', undefined, {
    idAttribute: (registrationServer: IRegistrationServer) => registrationServer.containerId
});

const workerManager: schema.Entity<IWorkerManager> = new schema.Entity('workerManagers', undefined, {
    idAttribute: (workerManager: IWorkerManager) => workerManager.id.toString()
});

const logs: schema.Entity<ILogs> = new schema.Entity('logs', undefined, {
    idAttribute: (logs: ILogs) => logs.eventId.toString()
});

app.define({appServices, appRules, appSimulatedMetrics});
service.define({apps, dependencies, dependents, serviceRules, serviceSimulatedMetrics});
container.define({containerRules, containerSimulatedMetrics});
cloudHost.define({hostRules, hostSimulatedMetrics});
edgeHost.define({hostRules, hostSimulatedMetrics});
ruleHost.define({conditions, edgeHosts, cloudHosts});
ruleApp.define({conditions, apps});
ruleService.define({conditions, services});
ruleContainer.define({conditions, containers});
condition.define({valueModes, fields, operators});
simulatedHostMetric.define({edgeHosts, cloudHosts});
simulatedAppMetric.define({apps});
simulatedServiceMetric.define({services});
simulatedContainerMetric.define({containers});

export const Schemas: ISchemas = {
    APP: app,
    APP_ARRAY: [app],
    APP_SERVICE: appService,
    APP_SERVICE_ARRAY: [appService],
    APP_RULE: ruleApp,
    APP_RULE_ARRAY: [ruleApp],
    APP_SIMULATED_METRIC: simulatedAppMetric,
    APP_SIMULATED_METRIC_ARRAY: [simulatedAppMetric],
    SERVICE: service,
    SERVICE_ARRAY: [service],
    SERVICE_APP: app,
    SERVICE_APP_ARRAY: [app],
    SERVICE_DEPENDENCY: dependency,
    SERVICE_DEPENDENCY_ARRAY: [dependency],
    SERVICE_DEPENDENT: dependent,
    SERVICE_DEPENDENT_ARRAY: [dependent],
    SERVICE_PREDICTION: prediction,
    SERVICE_PREDICTION_ARRAY: [prediction],
    SERVICE_RULE: ruleService,
    SERVICE_RULE_ARRAY: [ruleService],
    SERVICE_SIMULATED_METRIC: simulatedServiceMetric,
    SERVICE_SIMULATED_METRIC_ARRAY: [simulatedServiceMetric],
    CONTAINER: container,
    CONTAINER_ARRAY: [container],
    CONTAINER_RULE: ruleContainer,
    CONTAINER_RULE_ARRAY: [ruleContainer],
    CONTAINER_SIMULATED_METRIC: simulatedContainerMetric,
    CONTAINER_SIMULATED_METRIC_ARRAY: [simulatedContainerMetric],
    CLOUD_HOST: cloudHost,
    CLOUD_HOST_ARRAY: [cloudHost],
    CLOUD_HOST_RULE: ruleHost,
    CLOUD_HOST_RULE_ARRAY: [ruleHost],
    CLOUD_HOST_SIMULATED_METRIC: simulatedHostMetric,
    CLOUD_HOST_SIMULATED_METRIC_ARRAY: [simulatedHostMetric],
    EDGE_HOST: edgeHost,
    EDGE_HOST_ARRAY: [edgeHost],
    EDGE_HOST_RULE: ruleHost,
    EDGE_HOST_RULE_ARRAY: [ruleHost],
    EDGE_HOST_SIMULATED_METRIC: simulatedHostMetric,
    EDGE_HOST_SIMULATED_METRIC_ARRAY: [simulatedHostMetric],
    NODE: node,
    NODE_ARRAY: [node],
    REGION: region,
    REGION_ARRAY: [region],
    RULE_HOST: ruleHost,
    RULE_HOST_ARRAY: [ruleHost],
    RULE_APP: ruleApp,
    RULE_APP_ARRAY: [ruleApp],
    RULE_SERVICE: ruleService,
    RULE_SERVICE_ARRAY: [ruleService],
    RULE_CONTAINER: ruleContainer,
    RULE_CONTAINER_ARRAY: [ruleContainer],
    RULE_CONDITION: condition,
    RULE_CONDITION_ARRAY: [condition],
    VALUE_MODE_ARRAY: [valueMode],
    FIELD_ARRAY: [field],
    OPERATOR_ARRAY: [operator],
    DECISION: decision,
    DECISION_ARRAY: [decision],
    SIMULATED_HOST_METRIC: simulatedHostMetric,
    SIMULATED_HOST_METRIC_ARRAY: [simulatedHostMetric],
    SIMULATED_APP_METRIC: simulatedAppMetric,
    SIMULATED_APP_METRIC_ARRAY: [simulatedAppMetric],
    SIMULATED_SERVICE_METRIC: simulatedServiceMetric,
    SIMULATED_SERVICE_METRIC_ARRAY: [simulatedServiceMetric],
    SIMULATED_CONTAINER_METRIC: simulatedContainerMetric,
    SIMULATED_CONTAINER_METRIC_ARRAY: [simulatedContainerMetric],
    LOAD_BALANCER: loadBalancer,
    LOAD_BALANCER_ARRAY: [loadBalancer],
    REGISTRATION_SERVER: registrationServer,
    REGISTRATION_SERVER_ARRAY: [registrationServer],
    WORKER_MANAGER: workerManager,
    WORKER_MANAGER_ARRAY: [workerManager],
    LOGS_ARRAY: [logs],
};

export const CALL_API = 'Call API';

export default (store: any) => (next: (action: any) => void) => (action: any) => {
    const callAPI = action[CALL_API];
    if (typeof callAPI === 'undefined') {
        return next(action)
    }
    const {endpoint, schema, types, entity, method} = callAPI;
    const actionWith = (data: any) => {
        const finalAction = Object.assign({}, action, data);
        delete finalAction[CALL_API];
        return finalAction;
    };
    const [requestType, successType, failureType] = types;
    next(actionWith({type: requestType}));
    return callApi(endpoint, schema, method).then(
        response => {
            next(actionWith({type: successType, entity, data: response}));
        },
        error => {
            next(actionWith({type: failureType, error: error.message || 'Error fetching data'}));
        })

}
