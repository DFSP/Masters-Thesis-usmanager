package pt.unl.fct.miei.usmanagement.manager.services.rulesystem;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.AppRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ContainerRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules.ServiceRulesService;

import java.util.List;

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

	public RuleConditionsService(@Lazy HostRulesService hostRulesService, @Lazy AppRulesService appRulesService,
								 @Lazy ServiceRulesService serviceRulesService, @Lazy ContainerRulesService containerRulesService,
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

	public List<HostRuleCondition> getHostRuleConditions() {
		return hostRuleConditions.findAll();
	}

	@Transactional
	public void saveHostRuleCondition(HostRuleCondition hostRuleCondition) {
		Condition condition = hostRuleCondition.getCondition();
		conditionsService.saveCondition(condition);
		HostRule hostRule = hostRuleCondition.getRule();
		hostRulesService.saveRule(hostRule);
		hostRuleConditions.save(hostRuleCondition);
	}

	public List<AppRuleCondition> getAppRuleConditions() {
		return appRuleConditions.findAll();
	}

	@Transactional
	public void saveAppRuleCondition(AppRuleCondition appRuleCondition) {
		Condition condition = appRuleCondition.getCondition();
		conditionsService.saveCondition(condition);
		AppRule appRule = appRuleCondition.getRule();
		appRulesService.saveRule(appRule);
		appRuleConditions.save(appRuleCondition);
	}

	public List<ServiceRuleCondition> getServiceRuleConditions() {
		return serviceRuleConditions.findAll();
	}

	@Transactional
	public void saveServiceRuleCondition(ServiceRuleCondition serviceRuleCondition) {
		Condition condition = serviceRuleCondition.getCondition();
		conditionsService.addOrUpdateCondition(condition);
		ServiceRule serviceRule = serviceRuleCondition.getRule();
		serviceRulesService.addOrUpdateRule(serviceRule);
		serviceRuleConditions.save(serviceRuleCondition);
	}

	public List<ContainerRuleCondition> getContainerRuleConditions() {
		return containerRuleConditions.findAll();
	}

	@Transactional
	public void saveContainerRuleCondition(ContainerRuleCondition containerRuleCondition) {
		Condition condition = containerRuleCondition.getCondition();
		conditionsService.saveCondition(condition);
		ContainerRule containerRule = containerRuleCondition.getRule();
		containerRulesService.saveRule(containerRule);
		containerRuleConditions.save(containerRuleCondition);
	}

}
