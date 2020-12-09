package pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ContainerRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditions;

@Service
public class RuleConditionsService {

	private final HostRulesService hostRulesService;
	private final AppRulesService appRulesService;
	private final ServiceRulesService serviceRulesService;
	private final ContainerRulesService containerRulesService;
	private final ConditionsService conditionsService;

	private final HostRuleConditions hostRuleConditions;
	private final AppRuleConditions appRuleConditions;
	private final ServiceRuleConditions serviceRuleConditions;
	private final ContainerRuleConditions containerRuleConditions;

	public RuleConditionsService(HostRulesService hostRulesService, AppRulesService appRulesService,
								 ServiceRulesService serviceRulesService, ContainerRulesService containerRulesService,
								 ConditionsService conditionsService, HostRuleConditions hostRuleConditions, AppRuleConditions appRuleConditions,
								 ServiceRuleConditions serviceRuleConditions, ContainerRuleConditions containerRuleConditions) {
		this.hostRulesService = hostRulesService;
		this.appRulesService = appRulesService;
		this.serviceRulesService = serviceRulesService;
		this.containerRulesService = containerRulesService;
		this.conditionsService = conditionsService;
		this.hostRuleConditions = hostRuleConditions;
		this.appRuleConditions = appRuleConditions;
		this.serviceRuleConditions = serviceRuleConditions;
		this.containerRuleConditions = containerRuleConditions;
	}

	@Transactional
	public void saveHostRuleCondition(HostRuleCondition hostRuleCondition) {
		Condition condition = hostRuleCondition.getHostCondition();
		conditionsService.saveCondition(condition);
		HostRule hostRule = hostRuleCondition.getHostRule();
		hostRulesService.saveRule(hostRule);
		hostRuleConditions.save(hostRuleCondition);
	}

	@Transactional
	public void saveAppRuleCondition(AppRuleCondition appRuleCondition) {
		Condition condition = appRuleCondition.getAppCondition();
		conditionsService.saveCondition(condition);
		AppRule appRule = appRuleCondition.getAppRule();
		appRulesService.saveRule(appRule);
		appRuleConditions.save(appRuleCondition);
	}

	@Transactional
	public void saveServiceRuleCondition(ServiceRuleCondition serviceRuleCondition) {
		Condition condition = serviceRuleCondition.getServiceCondition();
		conditionsService.saveCondition(condition);
		ServiceRule serviceRule = serviceRuleCondition.getServiceRule();
		serviceRulesService.saveRule(serviceRule);
		serviceRuleConditions.save(serviceRuleCondition);
	}

	@Transactional
	public void saveContainerRuleCondition(ContainerRuleCondition containerRuleCondition) {
		Condition condition = containerRuleCondition.getContainerCondition();
		conditionsService.saveCondition(condition);
		ContainerRule containerRule = containerRuleCondition.getContainerRule();
		containerRulesService.saveRule(containerRule);
		containerRuleConditions.save(containerRuleCondition);
	}

}
