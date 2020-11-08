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

package pt.unl.fct.miei.usmanagement.manager.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.MasterManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServices;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencies;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.management.componenttypes.ComponentTypesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.management.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.management.operators.OperatorsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration.RegistrationServerService;
import pt.unl.fct.miei.usmanagement.manager.management.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.users.User;
import pt.unl.fct.miei.usmanagement.manager.users.UserRoleEnum;
import pt.unl.fct.miei.usmanagement.manager.users.UsersService;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DatabaseLoader {

	@Bean
	CommandLineRunner initDatabase(UsersService usersService,
								   ServicesService servicesService,
								   AppsService appsService, AppServices appServices,
								   ServiceDependencies servicesDependencies, /*RegionsService regionsService,*/
								   EdgeHostsService edgeHostsService, CloudHostsService cloudHostsService,
								   ComponentTypesService componentTypesService,
								   OperatorsService operatorsService, DecisionsService decisionsService,
								   FieldsService fieldsService, ValueModesService valueModesService,
								   ConditionsService conditionsService, HostRulesService hostRulesService,
								   HostRuleConditions hostRuleConditions,
								   ServiceRulesService serviceRulesService,
								   ServiceRuleConditions serviceRuleConditions,
								   DockerProperties dockerProperties, HostsEventsService hostsEventsService,
								   ServicesEventsService servicesEventsService, HostsMonitoringService hostsMonitoringService,
								   ServicesMonitoringService servicesMonitoringService) {
		return args -> {

			Map<String, User> users = loadUsers(usersService);

			String dockerHubUsername = dockerProperties.getHub().getUsername();
			Map<String, Service> services = new HashMap<>(loadSystemComponents(dockerHubUsername, servicesService, dockerProperties));
			Service registrationServer = services.get(RegistrationServerService.REGISTRATION_SERVER);
			services.putAll(loadSockShop(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadMixal(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadOnlineBoutique(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadHotelReservation(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadMediaMicroservices(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadSocialNetwork(dockerHubUsername, appsService, servicesService, servicesDependencies, appServices));
			services.putAll(loadTestingSuite(dockerHubUsername, appsService, servicesService, appServices));

			List<EdgeHost> edgeHosts = loadEdgeHosts(edgeHostsService);

			List<CloudHost> cloudHosts = loadCloudHosts(cloudHostsService);

			/*Map<RegionEnum, Region> regions = loadRegions(regionsService);*/

			Map<ComponentTypeEnum, ComponentType> componentTypes = loadComponentTypes(componentTypesService);

			Map<OperatorEnum, Operator> operators = loadOperators(operatorsService);

			Map<RuleDecisionEnum, Decision> decisions = loadDecisions(decisionsService, componentTypes);

			Map<String, Field> fields = loadFields(fieldsService);

			Map<String, ValueMode> valueModes = loadValueModes(valueModesService);

			Map<String, Condition> conditions = loadConditions(conditionsService, valueModes, fields, operators);

			Map<String, HostRule> hostRules = loadRules(hostRulesService, hostRuleConditions, decisions, conditions);

			Map<String, ServiceRule> serviceRules = loadServiceRules(serviceRulesService, serviceRuleConditions, decisions, conditions);

			hostsEventsService.reset();
			servicesEventsService.reset();

			hostsMonitoringService.reset();
			servicesMonitoringService.reset();
		};
	}

	private Map<String, ServiceRule> loadServiceRules(ServiceRulesService serviceRulesService,
													  ServiceRuleConditions serviceRuleConditions,
													  Map<RuleDecisionEnum, Decision> decisions,
													  Map<String, Condition> conditions) {
		Map<String, ServiceRule> serviceRuleMap = new HashMap<>();

		ServiceRule rxOver500000GenericServiceRule;
		try {
			rxOver500000GenericServiceRule = serviceRulesService.getRule("RxOver500000");
		}
		catch (EntityNotFoundException ignored) {
			Decision serviceDecisionReplicate = decisions.get(RuleDecisionEnum.REPLICATE);
			Condition rxBytesPerSecOver500000 = conditions.get("rxBytesPerSecOver500000");
			rxOver500000GenericServiceRule = ServiceRule.builder()
				.name("RxOver500000")
				.priority(1)
				.decision(serviceDecisionReplicate)
				.generic(true)
				.build();
			rxOver500000GenericServiceRule = serviceRulesService.addRule(rxOver500000GenericServiceRule);
			ServiceRuleCondition rxOver500000Condition = ServiceRuleCondition.builder()
				.serviceRule(rxOver500000GenericServiceRule)
				.serviceCondition(rxBytesPerSecOver500000)
				.build();
			serviceRuleConditions.save(rxOver500000Condition);
		}
		serviceRuleMap.put("RxOver500000", rxOver500000GenericServiceRule);

		return serviceRuleMap;
	}

	private Map<String, HostRule> loadRules(HostRulesService hostRulesService, HostRuleConditions hostRuleConditions,
											Map<RuleDecisionEnum, Decision> decisions, Map<String, Condition> conditions) {
		Map<String, HostRule> hostRuleMap = new HashMap<>();

		HostRule cpuAndRamOver90GenericHostRule;
		try {
			cpuAndRamOver90GenericHostRule = hostRulesService.getRule("CpuAndRamOver90");
		}
		catch (EntityNotFoundException ignored) {
			Decision hostDecisionOverwork = decisions.get(RuleDecisionEnum.OVERWORK);
			cpuAndRamOver90GenericHostRule = HostRule.builder()
				.name("CpuAndRamOver90")
				.priority(1)
				.decision(hostDecisionOverwork)
				.generic(true)
				.build();
			cpuAndRamOver90GenericHostRule = hostRulesService.addRule(cpuAndRamOver90GenericHostRule);

			Condition cpuPercentageOver90 = conditions.get("cpuPercentageOver90");
			HostRuleCondition cpuOver90Condition = HostRuleCondition.builder()
				.hostRule(cpuAndRamOver90GenericHostRule)
				.hostCondition(cpuPercentageOver90)
				.build();
			Condition ramPercentageOver90 = conditions.get("ramPercentageOver90");
			hostRuleConditions.save(cpuOver90Condition);
			HostRuleCondition ramOver90Condition = HostRuleCondition.builder()
				.hostRule(cpuAndRamOver90GenericHostRule)
				.hostCondition(ramPercentageOver90)
				.build();
			hostRuleConditions.save(ramOver90Condition);
		}
		hostRuleMap.put("CpuAndRamOver90", cpuAndRamOver90GenericHostRule);

		return hostRuleMap;
	}

	private Map<String, Condition> loadConditions(ConditionsService conditionsService, Map<String, ValueMode> valueModes,
												  Map<String, Field> fields, Map<OperatorEnum, Operator> operators) {
		Map<String, Condition> conditionsMap = new HashMap<>();

		ValueMode effectiveValue = valueModes.get("effective-val");
		Operator greaterThan = operators.get(OperatorEnum.GREATER_THAN);

		Condition cpuPercentageOver90;
		try {
			cpuPercentageOver90 = conditionsService.getCondition("CpuPercentageOver90");
		}
		catch (EntityNotFoundException ignored) {
			Field cpuPercentage = fields.get("cpu-%");
			cpuPercentageOver90 = Condition.builder()
				.name("CpuPercentageOver90")
				.valueMode(effectiveValue)
				.field(cpuPercentage)
				.operator(greaterThan)
				.value(90)
				.build();
			cpuPercentageOver90 = conditionsService.addCondition(cpuPercentageOver90);
		}
		conditionsMap.put("CpuPercentageOver90", cpuPercentageOver90);

		Condition ramPercentageOver90;
		try {
			ramPercentageOver90 = conditionsService.getCondition("RamPercentageOver90");
		}
		catch (EntityNotFoundException ignored) {
			Field ramPercentage = fields.get("ram-%");
			ramPercentageOver90 = Condition.builder()
				.name("RamPercentageOver90")
				.valueMode(effectiveValue)
				.field(ramPercentage)
				.operator(greaterThan)
				.value(90)
				.build();
			ramPercentageOver90 = conditionsService.addCondition(ramPercentageOver90);
		}
		conditionsMap.put("RamPercentageOver90", ramPercentageOver90);

		Condition rxBytesPerSecOver500000;
		try {
			rxBytesPerSecOver500000 = conditionsService.getCondition("RxBytesPerSecOver500000");
		}
		catch (EntityNotFoundException ignored) {
			Field rxBytesPerSec = fields.get("rx-bytes-per-sec");
			rxBytesPerSecOver500000 = Condition.builder()
				.name("RxBytesPerSecOver500000")
				.valueMode(effectiveValue)
				.field(rxBytesPerSec)
				.operator(greaterThan)
				.value(500000)
				.build();
			rxBytesPerSecOver500000 = conditionsService.addCondition(rxBytesPerSecOver500000);
		}
		conditionsMap.put("RxBytesPerSecOver500000", rxBytesPerSecOver500000);

		Condition txBytesPerSecOver100000;
		try {
			txBytesPerSecOver100000 = conditionsService.getCondition("TxBytesPerSecOver100000");
		}
		catch (EntityNotFoundException ignored) {
			Field txBytesPerSec = fields.get("tx-bytes-per-sec");
			txBytesPerSecOver100000 = Condition.builder()
				.name("TxBytesPerSecOver100000")
				.valueMode(effectiveValue)
				.field(txBytesPerSec)
				.operator(greaterThan)
				.value(100000)
				.build();
			txBytesPerSecOver100000 = conditionsService.addCondition(txBytesPerSecOver100000);
		}
		conditionsMap.put("TxBytesPerSecOver100000", txBytesPerSecOver100000);

		return conditionsMap;
	}

	private Map<String, ValueMode> loadValueModes(ValueModesService valueModesService) {
		Map<String, ValueMode> valueModes = new HashMap<>();

		ValueMode effectiveValue;
		try {
			effectiveValue = valueModesService.getValueMode("effective-val");
		}
		catch (EntityNotFoundException ignored) {
			effectiveValue = ValueMode.builder()
				.name("effective-val")
				.build();
			effectiveValue = valueModesService.addValueMode(effectiveValue);
		}
		valueModes.put("effective-val", effectiveValue);

		ValueMode averageValue;
		try {
			averageValue = valueModesService.getValueMode("avg-val");
		}
		catch (EntityNotFoundException ignored) {
			averageValue = ValueMode.builder()
				.name("avg-val")
				.build();
			averageValue = valueModesService.addValueMode(averageValue);
		}
		valueModes.put("avg-val", averageValue);

		ValueMode deviationPercentageOnAverageValue;
		try {
			deviationPercentageOnAverageValue = valueModesService.getValueMode("deviation-%-on-avg-val");
		}
		catch (EntityNotFoundException ignored) {
			deviationPercentageOnAverageValue = ValueMode.builder()
				.name("deviation-%-on-avg-val")
				.build();
			deviationPercentageOnAverageValue = valueModesService.addValueMode(deviationPercentageOnAverageValue);
		}
		valueModes.put("deviation-%-on-avg-val", deviationPercentageOnAverageValue);

		ValueMode deviationPercentageOnLastValue;
		try {
			deviationPercentageOnLastValue = valueModesService.getValueMode("deviation-%-on-last-val");
		}
		catch (EntityNotFoundException ignored) {
			deviationPercentageOnLastValue = ValueMode.builder()
				.name("deviation-%-on-last-val")
				.build();
			deviationPercentageOnLastValue = valueModesService.addValueMode(deviationPercentageOnLastValue);
		}
		valueModes.put("deviation-%-on-last-vall", deviationPercentageOnLastValue);

		return valueModes;
	}

	private Map<String, Field> loadFields(FieldsService fieldsService) {
		Map<String, Field> fieldsMap = new HashMap<>();

		Field cpu;
		try {
			cpu = fieldsService.getField("cpu");
		}
		catch (EntityNotFoundException ignored) {
			cpu = Field.builder()
				.name("cpu")
				.build();
			cpu = fieldsService.addField(cpu);
		}
		fieldsMap.put("cpu", cpu);

		Field ram;
		try {
			ram = fieldsService.getField("ram");
		}
		catch (EntityNotFoundException ignored) {
			ram = Field.builder()
				.name("ram")
				.query(PrometheusQueryEnum.MEMORY_USAGE)
				.build();
			ram = fieldsService.addField(ram);
		}
		fieldsMap.put("ram", ram);

		Field cpuPercentage;
		try {
			cpuPercentage = fieldsService.getField("cpu-%");
		}
		catch (EntityNotFoundException ignored) {
			cpuPercentage = Field.builder()
				.name("cpu-%")
				.query(PrometheusQueryEnum.CPU_USAGE_PERCENTAGE)
				.build();
			cpuPercentage = fieldsService.addField(cpuPercentage);
		}
		fieldsMap.put("cpu-%", cpuPercentage);

		Field ramPercentage;
		try {
			ramPercentage = fieldsService.getField("ram-%");
		}
		catch (EntityNotFoundException ignored) {
			ramPercentage = Field.builder()
				.name("ram-%")
				.query(PrometheusQueryEnum.MEMORY_USAGE_PERCENTAGE)
				.build();
			ramPercentage = fieldsService.addField(ramPercentage);
		}
		fieldsMap.put("ram-%", ramPercentage);

		Field rxBytes;
		try {
			rxBytes = fieldsService.getField("rx-bytes");
		}
		catch (EntityNotFoundException ignored) {
			rxBytes = Field.builder()
				.name("rx-bytes")
				.build();
			rxBytes = fieldsService.addField(rxBytes);
		}
		fieldsMap.put("rx-bytes", rxBytes);

		Field txBytes;
		try {
			txBytes = fieldsService.getField("tx-bytes");
		}
		catch (EntityNotFoundException ignored) {
			txBytes = Field.builder()
				.name("tx-bytes")
				.build();
			txBytes = fieldsService.addField(txBytes);
		}
		fieldsMap.put("tx-bytes", txBytes);

		Field rxBytesPerSec;
		try {
			rxBytesPerSec = fieldsService.getField("rx-bytes-per-sec");
		}
		catch (EntityNotFoundException ignored) {
			rxBytesPerSec = Field.builder()
				.name("rx-bytes-per-sec")
				.build();
			rxBytesPerSec = fieldsService.addField(rxBytesPerSec);
		}
		fieldsMap.put("rx-bytes-per-sec", rxBytesPerSec);

		Field txBytesPerSec;
		try {
			txBytesPerSec = fieldsService.getField("tx-bytes-per-sec");
		}
		catch (EntityNotFoundException ignored) {
			txBytesPerSec = Field.builder()
				.name("tx-bytes-per-sec")
				.build();
			txBytesPerSec = fieldsService.addField(txBytesPerSec);
		}
		fieldsMap.put("tx-bytes-per-sec", txBytesPerSec);

		Field latency;
		try {
			latency = fieldsService.getField("latency");
		}
		catch (EntityNotFoundException ignored) {
			latency = Field.builder()
				.name("latency")
				.build();
			latency = fieldsService.addField(latency);
		}
		fieldsMap.put("latency", latency);

		Field bandwidthPercentage;
		try {
			bandwidthPercentage = fieldsService.getField("bandwidth-%");
		}
		catch (EntityNotFoundException ignored) {
			bandwidthPercentage = Field.builder()
				.name("bandwidth-%")
				.build();
			bandwidthPercentage = fieldsService.addField(bandwidthPercentage);
		}
		fieldsMap.put("bandwidth-%", bandwidthPercentage);

		Field filesystemAvailableSpace;
		try {
			filesystemAvailableSpace = fieldsService.getField("filesystem-available-space");
		}
		catch (EntityNotFoundException ignored) {
			latency = Field.builder()
				.name("filesystem-available-space")
				.query(PrometheusQueryEnum.FILESYSTEM_AVAILABLE_SPACE)
				.build();
			filesystemAvailableSpace = fieldsService.addField(latency);
		}
		fieldsMap.put("filesystem-available-space", filesystemAvailableSpace);

		return fieldsMap;
	}

	private Map<RuleDecisionEnum, Decision> loadDecisions(DecisionsService decisionsService, Map<ComponentTypeEnum, ComponentType> componentTypes) {
		Map<RuleDecisionEnum, Decision> decisionsMap = new HashMap<>();

		ComponentType serviceComponentType = componentTypes.get(ComponentTypeEnum.SERVICE);

		Decision serviceDecisionNone;
		try {
			serviceDecisionNone = decisionsService.getServicePossibleDecision(RuleDecisionEnum.NONE);
		}
		catch (EntityNotFoundException ignored) {
			serviceDecisionNone = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(serviceComponentType)
				.ruleDecision(RuleDecisionEnum.NONE)
				.build();
			serviceDecisionNone = decisionsService.addDecision(serviceDecisionNone);
		}
		decisionsMap.put(RuleDecisionEnum.NONE, serviceDecisionNone);

		Decision serviceDecisionReplicate;
		try {
			serviceDecisionReplicate = decisionsService.getServicePossibleDecision(RuleDecisionEnum.REPLICATE);
		}
		catch (EntityNotFoundException ignored) {
			serviceDecisionReplicate = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(serviceComponentType)
				.ruleDecision(RuleDecisionEnum.REPLICATE)
				.build();
			serviceDecisionReplicate = decisionsService.addDecision(serviceDecisionReplicate);
		}
		decisionsMap.put(RuleDecisionEnum.REPLICATE, serviceDecisionReplicate);

		Decision serviceDecisionMigrate;
		try {
			serviceDecisionMigrate = decisionsService.getServicePossibleDecision(RuleDecisionEnum.MIGRATE);
		}
		catch (EntityNotFoundException ignored) {
			serviceDecisionMigrate = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(serviceComponentType)
				.ruleDecision(RuleDecisionEnum.MIGRATE)
				.build();
			serviceDecisionMigrate = decisionsService.addDecision(serviceDecisionMigrate);
		}
		decisionsMap.put(RuleDecisionEnum.MIGRATE, serviceDecisionMigrate);

		Decision serviceDecisionStop;
		try {
			serviceDecisionStop = decisionsService.getServicePossibleDecision(RuleDecisionEnum.STOP);
		}
		catch (EntityNotFoundException ignored) {
			serviceDecisionStop = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(serviceComponentType)
				.ruleDecision(RuleDecisionEnum.STOP)
				.build();
			serviceDecisionStop = decisionsService.addDecision(serviceDecisionStop);
		}
		decisionsMap.put(RuleDecisionEnum.STOP, serviceDecisionStop);

		ComponentType hostComponentType = componentTypes.get(ComponentTypeEnum.HOST);

		Decision hostDecisionNone;
		try {
			hostDecisionNone = decisionsService.getHostPossibleDecision(RuleDecisionEnum.NONE);
		}
		catch (EntityNotFoundException ignored) {
			hostDecisionNone = Decision.builder()
				.componentType(hostComponentType)
				.ruleDecision(RuleDecisionEnum.NONE)
				.build();
			hostDecisionNone = decisionsService.addDecision(hostDecisionNone);
		}
		decisionsMap.put(RuleDecisionEnum.NONE, hostDecisionNone);

		Decision hostDecisionStart;
		try {
			hostDecisionStart = decisionsService.getHostPossibleDecision(RuleDecisionEnum.OVERWORK);
		}
		catch (EntityNotFoundException ignored) {
			hostDecisionStart = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(hostComponentType)
				.ruleDecision(RuleDecisionEnum.OVERWORK)
				.build();
			hostDecisionStart = decisionsService.addDecision(hostDecisionStart);
		}
		decisionsMap.put(RuleDecisionEnum.OVERWORK, hostDecisionStart);

		Decision hostDecisionUnderwork;
		try {
			hostDecisionUnderwork = decisionsService.getHostPossibleDecision(RuleDecisionEnum.UNDERWORK);
		}
		catch (EntityNotFoundException ignored) {
			hostDecisionUnderwork = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
				.componentType(hostComponentType)
				.ruleDecision(RuleDecisionEnum.UNDERWORK)
				.build();
			hostDecisionUnderwork = decisionsService.addDecision(hostDecisionUnderwork);
		}
		decisionsMap.put(RuleDecisionEnum.UNDERWORK, hostDecisionUnderwork);

		return decisionsMap;
	}

	private Map<OperatorEnum, Operator> loadOperators(OperatorsService operatorsService) {
		Map<OperatorEnum, Operator> operatorsMap = new HashMap<>();

		Operator notEqualTo;
		try {
			notEqualTo = operatorsService.getOperator(OperatorEnum.NOT_EQUAL_TO);
		}
		catch (EntityNotFoundException ignored) {
			notEqualTo = Operator.builder()
				.operator(OperatorEnum.NOT_EQUAL_TO)
				.symbol(OperatorEnum.NOT_EQUAL_TO.getSymbol())
				.build();
			notEqualTo = operatorsService.addOperator(notEqualTo);
		}
		operatorsMap.put(OperatorEnum.NOT_EQUAL_TO, notEqualTo);

		Operator equalTo;
		try {
			equalTo = operatorsService.getOperator(OperatorEnum.EQUAL_TO);
		}
		catch (EntityNotFoundException ignored) {
			equalTo = Operator.builder()
				.operator(OperatorEnum.EQUAL_TO)
				.symbol(OperatorEnum.EQUAL_TO.getSymbol())
				.build();
			equalTo = operatorsService.addOperator(equalTo);
		}
		operatorsMap.put(OperatorEnum.EQUAL_TO, equalTo);

		Operator greaterThan;
		try {
			greaterThan = operatorsService.getOperator(OperatorEnum.GREATER_THAN);
		}
		catch (EntityNotFoundException ignored) {
			greaterThan = Operator.builder()
				.operator(OperatorEnum.GREATER_THAN)
				.symbol(OperatorEnum.GREATER_THAN.getSymbol())
				.build();
			greaterThan = operatorsService.addOperator(greaterThan);
		}
		operatorsMap.put(OperatorEnum.GREATER_THAN, greaterThan);

		Operator lessThan;
		try {
			lessThan = operatorsService.getOperator(OperatorEnum.LESS_THAN);
		}
		catch (EntityNotFoundException ignored) {
			lessThan = Operator.builder()
				.operator(OperatorEnum.LESS_THAN)
				.symbol(OperatorEnum.LESS_THAN.getSymbol())
				.build();
			lessThan = operatorsService.addOperator(lessThan);
		}
		operatorsMap.put(OperatorEnum.LESS_THAN, lessThan);

		Operator greaterThanOrEqualTo;
		try {
			greaterThanOrEqualTo = operatorsService.getOperator(OperatorEnum.GREATER_THAN_OR_EQUAL_TO);
		}
		catch (EntityNotFoundException ignored) {
			greaterThanOrEqualTo = Operator.builder()
				.operator(OperatorEnum.GREATER_THAN_OR_EQUAL_TO)
				.symbol(OperatorEnum.GREATER_THAN_OR_EQUAL_TO.getSymbol())
				.build();
			greaterThanOrEqualTo = operatorsService.addOperator(greaterThanOrEqualTo);
		}
		operatorsMap.put(OperatorEnum.GREATER_THAN_OR_EQUAL_TO, greaterThanOrEqualTo);

		Operator lessThanOrEqualTo;
		try {
			lessThanOrEqualTo = operatorsService.getOperator(OperatorEnum.LESS_THAN_OR_EQUAL_TO);
		}
		catch (EntityNotFoundException ignored) {
			lessThanOrEqualTo = Operator.builder()
				.operator(OperatorEnum.LESS_THAN_OR_EQUAL_TO)
				.symbol(OperatorEnum.LESS_THAN_OR_EQUAL_TO.getSymbol())
				.build();
			lessThanOrEqualTo = operatorsService.addOperator(lessThanOrEqualTo);
		}
		operatorsMap.put(OperatorEnum.LESS_THAN_OR_EQUAL_TO, lessThanOrEqualTo);

		return operatorsMap;
	}

	private Map<ComponentTypeEnum, ComponentType> loadComponentTypes(ComponentTypesService componentTypesService) {
		Map<ComponentTypeEnum, ComponentType> componentTypesMap = new HashMap<>();

		ComponentType hostComponentType;
		try {
			hostComponentType = componentTypesService.getComponentType(ComponentTypeEnum.HOST);
		}
		catch (EntityNotFoundException ignored) {
			hostComponentType = ComponentType.builder()
				.type(ComponentTypeEnum.HOST)
				.build();
			hostComponentType = componentTypesService.addComponentType(hostComponentType);
		}
		componentTypesMap.put(ComponentTypeEnum.HOST, hostComponentType);

		ComponentType serviceComponentType;
		try {
			serviceComponentType = componentTypesService.getComponentType(ComponentTypeEnum.SERVICE);
		}
		catch (EntityNotFoundException ignored) {
			serviceComponentType = ComponentType.builder()
				.type(ComponentTypeEnum.SERVICE)
				.build();
			serviceComponentType = componentTypesService.addComponentType(serviceComponentType);
		}
		componentTypesMap.put(ComponentTypeEnum.SERVICE, serviceComponentType);

		ComponentType container;
		try {
			container = componentTypesService.getComponentType(ComponentTypeEnum.CONTAINER);
		}
		catch (EntityNotFoundException ignored) {
			container = ComponentType.builder()
				.type(ComponentTypeEnum.CONTAINER)
				.build();
			container = componentTypesService.addComponentType(container);
		}
		componentTypesMap.put(ComponentTypeEnum.CONTAINER, container);

		return componentTypesMap;
	}

	private List<CloudHost> loadCloudHosts(CloudHostsService cloudHostsService) {
		return cloudHostsService.synchronizeDatabaseCloudHosts();
	}

	private List<EdgeHost> loadEdgeHosts(EdgeHostsService edgeHostsService) {
		List<EdgeHost> egeHosts = new ArrayList<>(1);

		EdgeHost danielHost;
		try {
			danielHost = edgeHostsService.getEdgeHostByDns("dpimenta.ddns.net");
		}
		catch (EntityNotFoundException ignored) {
			Coordinates coordinates = new Coordinates("Portugal", 39.575097, -8.909794);
			RegionEnum region = RegionEnum.getClosestRegion(coordinates);
			danielHost = edgeHostsService.addManualEdgeHost(EdgeHost.builder()
				.username("daniel")
				.publicIpAddress("2.82.208.89")
				.privateIpAddress("192.168.1.83")
				.publicDnsName("dpimenta.ddns.net")
				.region(region)
				.coordinates(coordinates)
				.build());
		}
		egeHosts.add(danielHost);

		return egeHosts;
	}

	/*private Map<RegionEnum, Region> loadRegions(RegionsService regionsService) {
		Map<RegionEnum, Region> regionsMap = new HashMap<>();

		for (RegionEnum regionEnum : RegionEnum.values()) {
			Region region;
			try {
				region = regionsService.getRegion(regionEnum);
			} catch (EntityNotFoundException ignored) {
				region = Region.builder()
					.region(region)
					.active(true)
					.build();
				region = regionsService.addRegion(region);
			}

			regionsMap.put(regionEnum, region);
		}

		return regionsMap;
	}*/


	private Map<String, Service> loadTestingSuite(String dockerHubUsername, AppsService appsService,
												  ServicesService servicesService, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(1);

		Service testingSuiteCrashTesting;
		try {
			testingSuiteCrashTesting = servicesService.getService("crash-testing");
		}
		catch (EntityNotFoundException ignored) {
			testingSuiteCrashTesting = Service.builder()
				.serviceName("crash-testing")
				.dockerRepository(dockerHubUsername + "/crash-testing")
				.defaultExternalPort(2500)
				.defaultInternalPort(80)
				.minimumReplicas(1)
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			testingSuiteCrashTesting = servicesService.addService(testingSuiteCrashTesting);
		}
		servicesMap.put("crash-testing", testingSuiteCrashTesting);

		if (!appsService.hasApp("Test suite")) {
			App testing = App.builder()
				.name("Test suite")
				.description("Microservices designed to test components of the system.")
				.build();
			testing = appsService.addApp(testing);
			appServices.saveAll(List.of(
				AppService.builder().app(testing).service(testingSuiteCrashTesting).build()));
		}

		return servicesMap;
	}

	private Map<String, Service> loadSocialNetwork(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
												   ServiceDependencies servicesDependencies, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(); // TODO capacity

		return servicesMap;
	}

	private Map<String, Service> loadMediaMicroservices(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
														ServiceDependencies servicesDependencies, AppServices appService) {
		Map<String, Service> servicesMap = new HashMap<>(); // TODO capacity

		return servicesMap;
	}

	private Map<String, Service> loadHotelReservation(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
													  ServiceDependencies servicesDependencies, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(); // TODO capacity

		return servicesMap;
	}

	private Map<String, Service> loadOnlineBoutique(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
													ServiceDependencies servicesDependencies, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(11);

		Service onlineBoutiqueAds;
		try {
			onlineBoutiqueAds = servicesService.getService("online-boutique-ads");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueAds = Service.builder()
				.serviceName("online-boutique-ads")
				.dockerRepository(dockerHubUsername + "/online-boutique-ads")
				.defaultExternalPort(9555)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${adsHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueAds = servicesService.addService(onlineBoutiqueAds);
		}
		servicesMap.put("online-boutique-ads", onlineBoutiqueAds);

		Service onlineBoutiqueCarts;
		try {
			onlineBoutiqueCarts = servicesService.getService("online-boutique-carts");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueCarts = Service.builder()
				.serviceName("online-boutique-carts")
				.dockerRepository(dockerHubUsername + "/online-boutique-carts")
				.defaultExternalPort(7070)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${cartsHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueCarts = servicesService.addService(onlineBoutiqueCarts);
		}
		servicesMap.put("online-boutique-carts", onlineBoutiqueCarts);

		Service onlineBoutiqueCheckout;
		try {
			onlineBoutiqueCheckout = servicesService.getService("online-boutique-checkout");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueCheckout = Service.builder()
				.serviceName("online-boutique-checkout")
				.dockerRepository(dockerHubUsername + "/online-boutique-checkout")
				.defaultExternalPort(5050)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${checkoutHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueCheckout = servicesService.addService(onlineBoutiqueCheckout);
		}
		servicesMap.put("online-boutique-checkout", onlineBoutiqueCheckout);

		Service onlineBoutiqueCurrency;
		try {
			onlineBoutiqueCurrency = servicesService.getService("online-boutique-currency");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueCurrency = Service.builder()
				.serviceName("online-boutique-currency")
				.dockerRepository(dockerHubUsername + "/online-boutique-currency")
				.defaultExternalPort(7000)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${currencyHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueCurrency = servicesService.addService(onlineBoutiqueCurrency);
		}
		servicesMap.put("online-boutique-currency", onlineBoutiqueCurrency);

		Service onlineBoutiqueEmail;
		try {
			onlineBoutiqueEmail = servicesService.getService("online-boutique-email");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueEmail = Service.builder()
				.serviceName("online-boutique-email")
				.dockerRepository(dockerHubUsername + "/online-boutique-email")
				.defaultExternalPort(8090)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${emailHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueEmail = servicesService.addService(onlineBoutiqueEmail);
		}
		servicesMap.put("online-boutique-email", onlineBoutiqueEmail);

		Service onlineBoutiqueFrontend;
		try {
			onlineBoutiqueFrontend = servicesService.getService("online-boutique-frontend");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueFrontend = Service.builder()
				.serviceName("online-boutique-frontend")
				.dockerRepository(dockerHubUsername + "/online-boutique-frontend")
				.defaultExternalPort(8090)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${frontendHost}")
				.serviceType(ServiceTypeEnum.FRONTEND)
				.build();
			onlineBoutiqueFrontend = servicesService.addService(onlineBoutiqueFrontend);
		}
		servicesMap.put("online-boutique-frontend", onlineBoutiqueFrontend);

		Service onlineBoutiqueLoadGenerator;
		try {
			onlineBoutiqueLoadGenerator = servicesService.getService("online-boutique-loadGenerator");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueLoadGenerator = Service.builder()
				.serviceName("online-boutique-loadGenerator")
				.dockerRepository(dockerHubUsername + "/online-boutique-loadGenerator")
				.defaultExternalPort(8090)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${loadGeneratorHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueLoadGenerator = servicesService.addService(onlineBoutiqueLoadGenerator);
		}
		servicesMap.put("online-boutique-loadGenerator", onlineBoutiqueLoadGenerator);

		Service onlineBoutiquePayment;
		try {
			onlineBoutiquePayment = servicesService.getService("online-boutique-payment");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiquePayment = Service.builder()
				.serviceName("online-boutique-payment")
				.dockerRepository(dockerHubUsername + "/online-boutique-payment")
				.defaultExternalPort(50051)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${paymentHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiquePayment = servicesService.addService(onlineBoutiquePayment);
		}
		servicesMap.put("online-boutique-payment", onlineBoutiquePayment);

		Service onlineBoutiqueCatalogue;
		try {
			onlineBoutiqueCatalogue = servicesService.getService("online-boutique-catalogue");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueCatalogue = Service.builder()
				.serviceName("online-boutique-catalogue")
				.dockerRepository(dockerHubUsername + "/online-boutique-catalogue")
				.defaultExternalPort(3550)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${catalogueHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueCatalogue = servicesService.addService(onlineBoutiqueCatalogue);
		}
		servicesMap.put("online-boutique-catalogue", onlineBoutiqueCatalogue);

		Service onlineBoutiqueRecommendation;
		try {
			onlineBoutiqueRecommendation = servicesService.getService("online-boutique-recommendation");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueRecommendation = Service.builder()
				.serviceName("online-boutique-recommendation")
				.dockerRepository(dockerHubUsername + "/online-boutique-recommendation")
				.defaultExternalPort(3550)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${recommendationHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueRecommendation = servicesService.addService(onlineBoutiqueRecommendation);
		}
		servicesMap.put("online-boutique-recommendation", onlineBoutiqueRecommendation);

		Service onlineBoutiqueShipping;
		try {
			onlineBoutiqueShipping = servicesService.getService("online-boutique-shipping");
		}
		catch (EntityNotFoundException ignored) {
			onlineBoutiqueShipping = Service.builder()
				.serviceName("online-boutique-shipping")
				.dockerRepository(dockerHubUsername + "/online-boutique-shipping")
				.defaultExternalPort(50055)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${shippingHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			onlineBoutiqueShipping = servicesService.addService(onlineBoutiqueShipping);
		}
		servicesMap.put("online-boutique-shipping", onlineBoutiqueShipping);
		
		if (!appsService.hasApp("Online Boutique")) {
			App onlineBoutique = App.builder()
				.name("Online Boutique")
				.description("Online Boutique is a cloud-native microservices demo application. " +
					"Online Boutique consists of a 10-tier microservices application. The application is a web-based " +
					"e-commerce app where users can browse items, add them to the cart, and purchase them.")
				.build();
			onlineBoutique = appsService.addApp(onlineBoutique);
			appServices.saveAll(List.of(
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueLoadGenerator).launchOrder(11).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueFrontend).launchOrder(10).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueAds).launchOrder(9).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueCheckout).launchOrder(8).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueShipping).launchOrder(7).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueCurrency).launchOrder(6).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueCatalogue).launchOrder(5).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueRecommendation).launchOrder(4).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueCarts).launchOrder(3).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiqueEmail).launchOrder(2).build(),
				AppService.builder().app(onlineBoutique).service(onlineBoutiquePayment).launchOrder(1).build()));
		}
			/*loadgeneration -> frontend
			frontend -> ad, checkout, shipping, currency, catalogue, recommendation, cartservice
			checkout -> email, payment, shipping, currency, cart
			recommendation -> catalogue*/

		return servicesMap;
	}

	private Map<String, Service> loadMixal(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
										   ServiceDependencies servicesDependencies, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(5);

		Service mixalPrime;
		try {
			mixalPrime = servicesService.getService("mixal-prime");
		}
		catch (EntityNotFoundException ignored) {
			mixalPrime = Service.builder()
				.serviceName("mixal-prime")
				.dockerRepository(dockerHubUsername + "/mixal-prime")
				.defaultExternalPort(9001)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${primeHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			mixalPrime = servicesService.addService(mixalPrime);
		}
		servicesMap.put("mixal-prime", mixalPrime);

		Service mixalMovie;
		try {
			mixalMovie = servicesService.getService("mixal-movie");
		}
		catch (EntityNotFoundException ignored) {
			mixalMovie = Service.builder()
				.serviceName("mixal-movie")
				.dockerRepository(dockerHubUsername + "/mixal-movie")
				.defaultExternalPort(9002)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${movieDatabaseHost}")
				.minimumReplicas(1)
				.outputLabel("${movieHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			mixalMovie = servicesService.addService(mixalMovie);
		}
		servicesMap.put("mixal-movie", mixalMovie);


		Service mixalMovieDb;
		try {
			mixalMovieDb = servicesService.getService("mixal-movie-db");
		}
		catch (EntityNotFoundException ignored) {
			mixalMovieDb = Service.builder()
				.serviceName("mixal-movie-db")
				.dockerRepository(dockerHubUsername + "/mixal-movie-db")
				.defaultExternalPort(28027)
				.defaultInternalPort(28027)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${movieDbHost}")
				.serviceType(ServiceTypeEnum.DATABASE)
				.build();
			mixalMovieDb = servicesService.addService(mixalMovieDb);
		}
		servicesMap.put("mixal-movie", mixalMovie);

		Service mixalServe;
		try {
			mixalServe = servicesService.getService("mixal-serve");
		}
		catch (EntityNotFoundException ignored) {
			mixalServe = Service.builder()
				.serviceName("mixal-serve")
				.dockerRepository(dockerHubUsername + "/mixal-serve")
				.defaultExternalPort(9003)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${serveHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.build();
			mixalServe = servicesService.addService(mixalServe);
		}
		servicesMap.put("mixal-serve", mixalServe);

		Service mixalWebac;
		try {
			mixalWebac = servicesService.getService("mixal-webac");
		}
		catch (EntityNotFoundException ignored) {
			mixalWebac = Service.builder()
				.serviceName("mixal-webac")
				.dockerRepository(dockerHubUsername + "/mixal-webac")
				.defaultExternalPort(9004)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${webacHost}")
				.serviceType(ServiceTypeEnum.FRONTEND)
				.build();
			mixalWebac = servicesService.addService(mixalWebac);
		}
		servicesMap.put("mixal-webac", mixalWebac);

		if (!appsService.hasApp("Mixal")) {
			App mixal = App.builder()
				.name("Mixal")
				.description("Set of four simple microservices.")
				.build();
			mixal = appsService.addApp(mixal);
			appServices.saveAll(List.of(
				AppService.builder().app(mixal).service(mixalWebac).launchOrder(5).build(),
				AppService.builder().app(mixal).service(mixalServe).launchOrder(4).build(),
				AppService.builder().app(mixal).service(mixalPrime).launchOrder(3).build(),
				AppService.builder().app(mixal).service(mixalMovie).launchOrder(2).build(),
				AppService.builder().app(mixal).service(mixalMovieDb).launchOrder(1).build()));
		}

		if (!servicesDependencies.hasDependency(mixalServe.getServiceName(), mixalMovie.getServiceName())) {
			ServiceDependency mixalServeMixalMovieDependency = ServiceDependency.builder()
				.service(mixalServe)
				.dependency(mixalMovie)
				.build();
			servicesDependencies.save(mixalServeMixalMovieDependency);
		}

		if (!servicesDependencies.hasDependency(mixalServe.getServiceName(), mixalPrime.getServiceName())) {
			ServiceDependency mixalServeMixalPrimeDependency = ServiceDependency.builder()
				.service(mixalServe)
				.dependency(mixalPrime)
				.build();
			servicesDependencies.save(mixalServeMixalPrimeDependency);
		}

		if (!servicesDependencies.hasDependency(mixalServe.getServiceName(), mixalWebac.getServiceName())) {
			ServiceDependency mixalServeMixalWebacDependency = ServiceDependency.builder()
				.service(mixalServe)
				.dependency(mixalWebac)
				.build();
			servicesDependencies.save(mixalServeMixalWebacDependency);
		}

		return servicesMap;
	}

	private Map<String, Service> loadSockShop(String dockerHubUsername, AppsService appsService, ServicesService servicesService,
											  ServiceDependencies servicesDependencies, AppServices appServices) {
		Map<String, Service> servicesMap = new HashMap<>(14);

		Service sockShopFrontend;
		try {
			sockShopFrontend = servicesService.getService("sock-shop-front-end");
		}
		catch (EntityNotFoundException ignored) {
			sockShopFrontend = Service.builder()
				.serviceName("sock-shop-front-end")
				.dockerRepository(dockerHubUsername + "/sock-shop-front-end")
				.defaultExternalPort(8081)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${frontendHost}")
				.serviceType(ServiceTypeEnum.FRONTEND)
				.expectedMemoryConsumption(209715200d)
				.build();
			sockShopFrontend = servicesService.addService(sockShopFrontend);
		}
		servicesMap.put("sock-shop-front-end", sockShopFrontend);

		Service sockShopUser;
		try {
			sockShopUser = servicesService.getService("sock-shop-user");
		}
		catch (EntityNotFoundException ignored) {
			sockShopUser = Service.builder()
				.serviceName("sock-shop-user")
				.dockerRepository(dockerHubUsername + "/sock-shop-user")
				.defaultExternalPort(8082)
				.defaultInternalPort(80)
				.defaultDb("user-db:27017")
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${userDatabaseHost}")
				.minimumReplicas(1)
				.outputLabel("${userHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(62914560d)
				.build();
			sockShopUser = servicesService.addService(sockShopUser);
		}
		servicesMap.put("sock-shop-user", sockShopUser);

		Service sockShopUserDb;
		try {
			sockShopUserDb = servicesService.getService("sock-shop-user-db");
		}
		catch (EntityNotFoundException ignored) {
			sockShopUserDb = Service.builder()
				.serviceName("sock-shop-user-db")
				.dockerRepository(dockerHubUsername + "/sock-shop-user-db")
				.defaultExternalPort(27017)
				.defaultInternalPort(27017)
				.minimumReplicas(1)
				.outputLabel("${userDatabaseHost}")
				.serviceType(ServiceTypeEnum.DATABASE)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopUserDb = servicesService.addService(sockShopUserDb);
		}
		servicesMap.put("sock-shop-user-db", sockShopUserDb);

		Service sockShopCatalogue;
		try {
			sockShopCatalogue = servicesService.getService("sock-shop-catalogue");
		}
		catch (EntityNotFoundException ignored) {
			sockShopCatalogue = Service.builder()
				.serviceName("sock-shop-catalogue")
				.dockerRepository(dockerHubUsername + "/sock-shop-catalogue")
				.defaultExternalPort(8083)
				.defaultInternalPort(80)
				.defaultDb("catalogue-db:3306")
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${catalogueDatabaseHost}")
				.minimumReplicas(1)
				.outputLabel("${catalogueHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(62914560d)
				.build();
			sockShopCatalogue = servicesService.addService(sockShopCatalogue);
		}
		servicesMap.put("sock-shop-catalogue", sockShopCatalogue);

		Service sockShopCatalogueDb;
		try {
			sockShopCatalogueDb = servicesService.getService("sock-shop-catalogue-db");
		}
		catch (EntityNotFoundException ignored) {
			sockShopCatalogueDb = Service.builder()
				.serviceName("sock-shop-catalogue-db")
				.dockerRepository(dockerHubUsername + "/sock-shop-catalogue-db")
				.defaultExternalPort(3306)
				.defaultInternalPort(3306)
				.minimumReplicas(1)
				.outputLabel("${catalogueDatabaseHost}")
				.serviceType(ServiceTypeEnum.DATABASE)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopCatalogueDb = servicesService.addService(sockShopCatalogueDb);
		}
		servicesMap.put("sock-shop-catalogue-db", sockShopCatalogueDb);

		Service sockShopPayment;
		try {
			sockShopPayment = servicesService.getService("sock-shop-payment");
		}
		catch (EntityNotFoundException ignored) {
			sockShopPayment = Service.builder()
				.serviceName("sock-shop-payment")
				.dockerRepository(dockerHubUsername + "/sock-shop-payment")
				.defaultExternalPort(8084)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.outputLabel("${paymentHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(62914560d)
				.build();
			sockShopPayment = servicesService.addService(sockShopPayment);
		}
		servicesMap.put("sock-shop-payment", sockShopPayment);

		Service sockShopCarts;
		try {
			sockShopCarts = servicesService.getService("sock-shop-carts");
		}
		catch (EntityNotFoundException ignored) {
			sockShopCarts = Service.builder()
				.serviceName("sock-shop-carts")
				.dockerRepository(dockerHubUsername + "/sock-shop-carts")
				.defaultExternalPort(8085)
				.defaultInternalPort(80)
				.defaultDb("carts-db:27017")
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${cartsDatabaseHost}")
				.minimumReplicas(1)
				.outputLabel("${cartsHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopCarts = servicesService.addService(sockShopCarts);
		}
		servicesMap.put("sock-shop-carts", sockShopCarts);

		Service sockShopCartsDb;
		try {
			sockShopCartsDb = servicesService.getService("sock-shop-carts-db");
		}
		catch (EntityNotFoundException ignored) {
			sockShopCartsDb = Service.builder()
				.serviceName("sock-shop-carts-db")
				.dockerRepository(dockerHubUsername + "/sock-shop-carts-db")
				.defaultExternalPort(27016)
				.defaultInternalPort(27017)
				.minimumReplicas(1)
				.outputLabel("${cartsDatabaseHost}")
				.serviceType(ServiceTypeEnum.DATABASE)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopCartsDb = servicesService.addService(sockShopCartsDb);
		}
		servicesMap.put("sock-shop-carts-db", sockShopCartsDb);

		Service sockShopOrders;
		try {
			sockShopOrders = servicesService.getService("sock-shop-orders");
		}
		catch (EntityNotFoundException ignored) {
			sockShopOrders = Service.builder()
				.serviceName("sock-shop-orders")
				.dockerRepository(dockerHubUsername + "/sock-shop-orders")
				.defaultExternalPort(8086)
				.defaultInternalPort(80)
				.defaultDb("orders-db:27017")
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${ordersDatabaseHost}")
				.minimumReplicas(1)
				.outputLabel("${ordersHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopOrders = servicesService.addService(sockShopOrders);
		}
		servicesMap.put("sock-shop-orders", sockShopOrders);

		Service sockShopOrdersDb;
		try {
			sockShopOrdersDb = servicesService.getService("sock-shop-orders-db");
		}
		catch (EntityNotFoundException ignored) {
			sockShopOrdersDb = Service.builder()
				.serviceName("sock-shop-orders-db")
				.dockerRepository(dockerHubUsername + "/sock-shop-orders-db")
				.defaultExternalPort(27015)
				.defaultInternalPort(27017)
				.minimumReplicas(1)
				.outputLabel("${ordersDatabaseHost}")
				.serviceType(ServiceTypeEnum.DATABASE)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopOrdersDb = servicesService.addService(sockShopOrdersDb);
		}
		servicesMap.put("sock-shop-orders-db", sockShopOrdersDb);

		Service sockShopShipping;
		try {
			sockShopShipping = servicesService.getService("sock-shop-shipping");
		}
		catch (EntityNotFoundException ignored) {
			sockShopShipping = Service.builder()
				.serviceName("sock-shop-shipping")
				.dockerRepository(dockerHubUsername + "/sock-shop-shipping")
				.defaultExternalPort(8087)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
				.minimumReplicas(1)
				.outputLabel("${shippingHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopShipping = servicesService.addService(sockShopShipping);
		}
		servicesMap.put("sock-shop-shipping", sockShopShipping);

		Service sockShopQueueMaster;
		try {
			sockShopQueueMaster = servicesService.getService("sock-shop-queue-master");
		}
		catch (EntityNotFoundException ignored) {
			sockShopQueueMaster = Service.builder()
				.serviceName("sock-shop-queue-master")
				.dockerRepository(dockerHubUsername + "/sock-shop-queue-master")
				.defaultExternalPort(8088)
				.defaultInternalPort(80)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
				.minimumReplicas(1)
				.outputLabel("${queue-masterHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopQueueMaster = servicesService.addService(sockShopQueueMaster);
		}
		servicesMap.put("sock-shop-queue-master", sockShopQueueMaster);

		Service sockShopRabbitmq;
		try {
			sockShopRabbitmq = servicesService.getService("sock-shop-rabbitmq");
		}
		catch (EntityNotFoundException ignored) {
			sockShopRabbitmq = Service.builder()
				.serviceName("sock-shop-rabbitmq")
				.dockerRepository(dockerHubUsername + "/sock-shop-rabbitmq")
				.defaultExternalPort(5672)
				.defaultInternalPort(5672)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.maximumReplicas(1)
				.outputLabel("${rabbitmqHost}")
				.serviceType(ServiceTypeEnum.BACKEND)
				.expectedMemoryConsumption(262144000d)
				.build();
			sockShopRabbitmq = servicesService.addService(sockShopRabbitmq);
		}
		servicesMap.put("sock-shop-rabbitmq", sockShopRabbitmq);

		if (!appsService.hasApp("Sock Shop")) {
			App sockShop = App.builder()
				.name("Sock Shop")
				.description("Sock Shop simulates the user-facing part of an e-commerce website that sells socks. " +
					"It is intended to aid the demonstration and testing of microservice and cloud native technologies.")
				.build();
			sockShop = appsService.addApp(sockShop);
			appServices.saveAll(List.of(
				AppService.builder().app(sockShop).service(sockShopFrontend).launchOrder(25).build(),
				AppService.builder().app(sockShop).service(sockShopUser).launchOrder(10).build(),
				AppService.builder().app(sockShop).service(sockShopUserDb).launchOrder(0).build(),
				AppService.builder().app(sockShop).service(sockShopCatalogue).launchOrder(5).build(),
				AppService.builder().app(sockShop).service(sockShopCatalogueDb).launchOrder(0).build(),
				AppService.builder().app(sockShop).service(sockShopPayment).launchOrder(5).build(),
				AppService.builder().app(sockShop).service(sockShopCarts).launchOrder(10).build(),
				AppService.builder().app(sockShop).service(sockShopCartsDb).launchOrder(0).build(),
				AppService.builder().app(sockShop).service(sockShopOrders).launchOrder(20).build(),
				AppService.builder().app(sockShop).service(sockShopOrdersDb).launchOrder(0).build(),
				AppService.builder().app(sockShop).service(sockShopShipping).launchOrder(15).build(),
				AppService.builder().app(sockShop).service(sockShopQueueMaster).launchOrder(15).build(),
				AppService.builder().app(sockShop).service(sockShopRabbitmq).launchOrder(5).build()));
		}

		if (!servicesDependencies.hasDependency(sockShopFrontend.getServiceName(), sockShopUser.getServiceName())) {
			ServiceDependency frontendUserDependency = ServiceDependency.builder()
				.service(sockShopFrontend)
				.dependency(sockShopUser)
				.build();
			servicesDependencies.save(frontendUserDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopFrontend.getServiceName(), sockShopCatalogue.getServiceName())) {
			ServiceDependency frontendCatalogueDependency = ServiceDependency.builder()
				.service(sockShopFrontend)
				.dependency(sockShopCatalogue)
				.build();
			servicesDependencies.save(frontendCatalogueDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopFrontend.getServiceName(), sockShopPayment.getServiceName())) {
			ServiceDependency frontendPaymentDependency = ServiceDependency.builder()
				.service(sockShopFrontend)
				.dependency(sockShopPayment)
				.build();
			servicesDependencies.save(frontendPaymentDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopFrontend.getServiceName(), sockShopCarts.getServiceName())) {
			ServiceDependency frontendCartsDependency = ServiceDependency.builder()
				.service(sockShopFrontend)
				.dependency(sockShopCarts)
				.build();
			servicesDependencies.save(frontendCartsDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopFrontend.getServiceName(), sockShopOrders.getServiceName())) {
			ServiceDependency frontendOrdersDependency = ServiceDependency.builder()
				.service(sockShopFrontend)
				.dependency(sockShopOrders)
				.build();
			servicesDependencies.save(frontendOrdersDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopUser.getServiceName(), sockShopUserDb.getServiceName())) {
			ServiceDependency userUserDbDependency = ServiceDependency.builder()
				.service(sockShopUser)
				.dependency(sockShopUserDb)
				.build();
			servicesDependencies.save(userUserDbDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopCatalogue.getServiceName(), sockShopCatalogueDb.getServiceName())) {
			ServiceDependency catalogueCatalogueDbDependency = ServiceDependency.builder()
				.service(sockShopCatalogue)
				.dependency(sockShopCatalogueDb)
				.build();
			servicesDependencies.save(catalogueCatalogueDbDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopCarts.getServiceName(), sockShopCartsDb.getServiceName())) {
			ServiceDependency cartsCartsDbDependency = ServiceDependency.builder()
				.service(sockShopCarts)
				.dependency(sockShopCartsDb)
				.build();
			servicesDependencies.save(cartsCartsDbDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopOrders.getServiceName(), sockShopPayment.getServiceName())) {
			ServiceDependency ordersPaymentDependency = ServiceDependency.builder()
				.service(sockShopOrders)
				.dependency(sockShopPayment)
				.build();
			servicesDependencies.save(ordersPaymentDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopOrders.getServiceName(), sockShopShipping.getServiceName())) {
			ServiceDependency ordersShippingDependency = ServiceDependency.builder()
				.service(sockShopOrders)
				.dependency(sockShopShipping)
				.build();
			servicesDependencies.save(ordersShippingDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopOrders.getServiceName(), sockShopOrdersDb.getServiceName())) {
			ServiceDependency ordersOrdersDbDependency = ServiceDependency.builder()
				.service(sockShopOrders)
				.dependency(sockShopOrdersDb)
				.build();
			servicesDependencies.save(ordersOrdersDbDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopShipping.getServiceName(), sockShopRabbitmq.getServiceName())) {
			ServiceDependency shippingRabbitmqDependency = ServiceDependency.builder()
				.service(sockShopShipping)
				.dependency(sockShopRabbitmq)
				.build();
			servicesDependencies.save(shippingRabbitmqDependency);
		}
		if (!servicesDependencies.hasDependency(sockShopQueueMaster.getServiceName(), sockShopRabbitmq.getServiceName())) {
			ServiceDependency queueMasterRabbitmqDependency = ServiceDependency.builder()
				.service(sockShopQueueMaster)
				.dependency(sockShopRabbitmq)
				.build();
			servicesDependencies.save(queueMasterRabbitmqDependency);
		}

		return servicesMap;
	}

	private Map<String, User> loadUsers(UsersService usersService) {
		Map<String, User> usersMap = new HashMap<>(2);

		User admin;
		try {
			admin = usersService.getUser("admin");
		}
		catch (EntityNotFoundException e) {
			admin = User.builder()
				.firstName("admin")
				.lastName("admin")
				.username("admin")
				.password("admin")
				.email("admin@admin.pt")
				.role(UserRoleEnum.ROLE_SYS_ADMIN)
				.build();
			admin = usersService.addUser(admin);
		}
		usersMap.put("admin", admin);

		User danielfct;
		try {
			danielfct = usersService.getUser("danielfct");
		}
		catch (EntityNotFoundException e) {
			danielfct = User.builder()
				.firstName("daniel")
				.lastName("pimenta")
				.username("danielfct")
				.password("danielfct")
				.email("d.pimenta@campus.fct.unl.pt")
				.role(UserRoleEnum.ROLE_SYS_ADMIN)
				.build();
			danielfct = usersService.addUser(danielfct);
		}
		usersMap.put("danielfct", danielfct);

		return usersMap;
	}

	private Map<String, Service> loadSystemComponents(String dockerHubUsername, ServicesService servicesService, DockerProperties dockerProperties) {
		Map<String, Service> servicesMap = new HashMap<>(7);

		Service masterManager;
		try {
			masterManager = servicesService.getService(MasterManagerProperties.MASTER_MANAGER);
		}
		catch (EntityNotFoundException ignored) {
			masterManager = Service.builder()
				.serviceName(MasterManagerProperties.MASTER_MANAGER)
				.dockerRepository(dockerHubUsername + "/manager-master")
				.defaultExternalPort(8080)
				.defaultInternalPort(8080)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.minimumReplicas(1)
				.maximumReplicas(1)
				.outputLabel("${masterManagerHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.build();
			masterManager = servicesService.addService(masterManager);
		}
		servicesMap.put(MasterManagerProperties.MASTER_MANAGER, masterManager);

		Service workerManager;
		try {
			workerManager = servicesService.getService(WorkerManagerProperties.WORKER_MANAGER);
		}
		catch (EntityNotFoundException ignored) {
			workerManager = Service.builder()
				.serviceName(WorkerManagerProperties.WORKER_MANAGER)
				.dockerRepository(dockerHubUsername + "/manager-worker")
				.defaultExternalPort(8081)
				.defaultInternalPort(8081)
				.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
				.outputLabel("${workerManagerHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.build();
			workerManager = servicesService.addService(workerManager);
		}
		servicesMap.put(WorkerManagerProperties.WORKER_MANAGER, workerManager);

		Service dockerApiProxy;
		try {
			dockerApiProxy = servicesService.getService(DockerApiProxyService.DOCKER_API_PROXY);
		}
		catch (EntityNotFoundException ignored) {
			dockerApiProxy = Service.builder()
				.serviceName(DockerApiProxyService.DOCKER_API_PROXY)
				.dockerRepository(dockerHubUsername + "/nginx-basic-auth-proxy")
				.defaultExternalPort(dockerProperties.getApiProxy().getPort())
				.defaultInternalPort(80)
				.minimumReplicas(1)
				.outputLabel("${dockerApiProxyHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.expectedMemoryConsumption(10485760d)
				.build();
			dockerApiProxy = servicesService.addService(dockerApiProxy);
		}
		servicesMap.put(DockerApiProxyService.DOCKER_API_PROXY, dockerApiProxy);

		Service loadBalancer;
		try {
			loadBalancer = servicesService.getService(NginxLoadBalancerService.LOAD_BALANCER);
		}
		catch (EntityNotFoundException ignored) {
			loadBalancer = Service.builder()
				.serviceName(NginxLoadBalancerService.LOAD_BALANCER)
				.dockerRepository(dockerHubUsername + "/nginx-load-balancer")
				.defaultExternalPort(1906)
				.defaultInternalPort(80)
				.minimumReplicas(1)
				.outputLabel("${loadBalancerHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.expectedMemoryConsumption(10485760d)
				.build();
			loadBalancer = servicesService.addService(loadBalancer);
		}
		servicesMap.put(NginxLoadBalancerService.LOAD_BALANCER, loadBalancer);

		Service requestLocationMonitor;
		try {
			requestLocationMonitor = servicesService.getService(LocationRequestsService.REQUEST_LOCATION_MONITOR);
		}
		catch (EntityNotFoundException ignored) {
			requestLocationMonitor = Service.builder()
				.serviceName(LocationRequestsService.REQUEST_LOCATION_MONITOR)
				.dockerRepository(dockerHubUsername + "/request-location-monitor")
				.defaultExternalPort(1919)
				.defaultInternalPort(1919)
				.minimumReplicas(1)
				.outputLabel("${requestLocationMonitorHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.expectedMemoryConsumption(52428800d)
				.build();
			requestLocationMonitor = servicesService.addService(requestLocationMonitor);
		}
		servicesMap.put(LocationRequestsService.REQUEST_LOCATION_MONITOR, requestLocationMonitor);

		Service registrationServer;
		try {
			registrationServer = servicesService.getService(RegistrationServerService.REGISTRATION_SERVER);
		}
		catch (EntityNotFoundException ignored) {
			registrationServer = Service.builder()
				.serviceName(RegistrationServerService.REGISTRATION_SERVER)
				.dockerRepository(dockerHubUsername + "/registration-server")
				.defaultExternalPort(8761)
				.defaultInternalPort(8761)
				.launchCommand("${externalPort} ${internalPort} ${hostname} ${zone}")
				.minimumReplicas(1)
				.outputLabel("${registrationHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.expectedMemoryConsumption(262144000d)
				.build();
			registrationServer = servicesService.addService(registrationServer);
		}
		servicesMap.put(RegistrationServerService.REGISTRATION_SERVER, registrationServer);

		Service prometheus;
		try {
			prometheus = servicesService.getService(PrometheusService.PROMETHEUS);
		}
		catch (EntityNotFoundException ignored) {
			prometheus = Service.builder()
				.serviceName(PrometheusService.PROMETHEUS)
				.dockerRepository(dockerHubUsername + "/prometheus")
				.defaultExternalPort(9090)
				.defaultInternalPort(9090)
				.minimumReplicas(1)
				.outputLabel("${prometheusHost}")
				.serviceType(ServiceTypeEnum.SYSTEM)
				.expectedMemoryConsumption(52428800d)
				.build();
			prometheus = servicesService.addService(prometheus);
		}
		servicesMap.put(PrometheusService.PROMETHEUS, prometheus);

		return servicesMap;
	}

}
