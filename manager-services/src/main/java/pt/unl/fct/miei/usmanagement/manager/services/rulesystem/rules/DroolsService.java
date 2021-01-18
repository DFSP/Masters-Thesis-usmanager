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

package pt.unl.fct.miei.usmanagement.manager.services.rulesystem.rules;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultAgendaEventListener;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.Match;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.ContainerEvent;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.Event;
import pt.unl.fct.miei.usmanagement.manager.services.monitoring.events.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.HostDecisionResult;
import pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision.ServiceDecisionResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DroolsService {

	private final Map<String, Long> lastUpdateServiceRules;
	private final Map<HostAddress, Long> lastUpdateHostRules;
	private final Map<String, StatelessKieSession> serviceRuleSessions;
	private final Map<HostAddress, StatelessKieSession> hostRuleSessions;

	public DroolsService() {
		this.lastUpdateServiceRules = new HashMap<>();
		this.lastUpdateHostRules = new HashMap<>();
		this.serviceRuleSessions = new HashMap<>();
		this.hostRuleSessions = new HashMap<>();
	}

	public boolean shouldCreateNewServiceRuleSession(String serviceName, long lastUpdate) {
		final long currVal = lastUpdateServiceRules.getOrDefault(serviceName, -1L);
		if (currVal < lastUpdate) {
			lastUpdateServiceRules.put(serviceName, lastUpdate);
			return true;
		}
		return false;
	}

	public boolean shouldCreateNewHostRuleSession(HostAddress hostAddress, long lastUpdate) {
		long currentTimestamp = lastUpdateHostRules.getOrDefault(hostAddress, -1L);
		if (currentTimestamp < lastUpdate) {
			lastUpdateHostRules.put(hostAddress, lastUpdate);
			return true;
		}
		return false;
	}

	public void createNewServiceRuleSession(String serviceName, Map<Long, String> drls) {
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for (Map.Entry<Long, String> drl : drls.entrySet()) {
			Resource resource = kieServices.getResources().newByteArrayResource(drl.getValue().getBytes());
			//TODO
			resource.setTargetPath("rules/rule_container_" + drl.getKey() + ".drl");
			kieFileSystem.write(resource);
		}
		kieServices.newKieBuilder(kieFileSystem).buildAll();
		ReleaseId releaseId = kieServices.getRepository().getDefaultReleaseId();
		StatelessKieSession serviceRuleSession = kieServices
			.newKieContainer(releaseId).getKieBase().newStatelessKieSession();
		TrackingAgendaEventListener agendaEventListener = new TrackingAgendaEventListener();
		serviceRuleSession.addEventListener(agendaEventListener);
		serviceRuleSessions.put(serviceName, serviceRuleSession);
	}

	public void createNewHostRuleSession(HostAddress hostAddress, Map<Long, String> drools) {
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for (Map.Entry<Long, String> drl : drools.entrySet()) {
			Resource resource = kieServices.getResources().newByteArrayResource(drl.getValue().getBytes());
			resource.setTargetPath("rules/rule_host_" + drl.getKey() + ".drl");
			kieFileSystem.write(resource);
		}
		kieServices.newKieBuilder(kieFileSystem).buildAll();
		ReleaseId releaseId = kieServices.getRepository().getDefaultReleaseId();
		StatelessKieSession hostRuleSession = kieServices
			.newKieContainer(releaseId).getKieBase().newStatelessKieSession();
		TrackingAgendaEventListener agendaEventListener = new TrackingAgendaEventListener();
		hostRuleSession.addEventListener(agendaEventListener);
		hostRuleSessions.put(hostAddress, hostRuleSession);
	}

	public Map<Long, String> executeDroolsRules(Event event, List<Rule> rules, String templateFile) {
		Map<Long, String> droolsRules = new HashMap<>();
		for (Rule rule : rules) {
			String droolsRule = applyRuleTemplate(event, rule, templateFile);
			droolsRules.put(rule.getId(), droolsRule);
		}
		return droolsRules;
	}

	private String applyRuleTemplate(Event event, Rule rule, String templateFile) {
		Map<String, Object> data = Map.of(
			"ruleId", rule.getId(),
			"rule", rule,
			"eventType", event.getClass().getName(),
			"decision", RuleDecisionEnum.class.getCanonicalName() + "." + rule.getDecision().toString(),
			"priority", rule.getPriority());
		log.info("Generated rule " + data.toString());
		ByteArrayInputStream ruleTemplate = new ByteArrayInputStream(getRuleTemplate(templateFile).getBytes(StandardCharsets.UTF_8));
		return new ObjectDataCompiler().compile(List.of(data), ruleTemplate);
	}

	@Cacheable(cacheNames = "ruleTemplateCache", key = "#templateFile")
	public String getRuleTemplate(String templateFile) {
		log.info("Generating rule template...");
		InputStream ruleTemplate = Thread.currentThread().getContextClassLoader().getResourceAsStream(templateFile);
		assert ruleTemplate != null;
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		try {
			int length;
			while ((length = ruleTemplate.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
		}
		catch (IOException e) {
			log.error("Failed to create rule template {}: {}", templateFile, e.getMessage());
		}
		return result.toString(StandardCharsets.UTF_8);
	}

	public ServiceDecisionResult evaluate(HostAddress hostAddress, ContainerEvent event) {
		Decision containerDecision = new Decision();
		String serviceName = event.getServiceName();
		StatelessKieSession serviceRuleSession = serviceRuleSessions.get(serviceName);
		serviceRuleSession.getGlobals().set("containerDecision", containerDecision);
		serviceRuleSession.execute(event);
		long ruleId = getRuleFired(new ArrayList<>(serviceRuleSession.getAgendaEventListeners()));
		return new ServiceDecisionResult(hostAddress, event.getContainerId(), event.getServiceName(),
			containerDecision.getDecision(), ruleId, event.getFields(), containerDecision.getPriority());
	}

	public HostDecisionResult evaluate(HostEvent event) {
		Decision hostDecision = new Decision();
		HostAddress hostAddress = event.getHostAddress();
		StatelessKieSession hostRuleSession = hostRuleSessions.get(hostAddress);
		hostRuleSession.getGlobals().set("hostDecision", hostDecision);
		hostRuleSession.execute(event);
		long ruleId = getRuleFired(hostRuleSession.getAgendaEventListeners());
		return new HostDecisionResult(hostAddress, hostDecision.getDecision(), ruleId, event.getFields(), hostDecision.getPriority());
	}

	private long getRuleFired(Collection<AgendaEventListener> agendaEvents) {
		Optional<AgendaEventListener> agendaEvent = agendaEvents.stream().findFirst();
		if (agendaEvent.isEmpty()) {
			return 0;
		}
		TrackingAgendaEventListener trackingAgendaEvent = (TrackingAgendaEventListener) agendaEvent.get();
		List<Match> matchList = trackingAgendaEvent.getMatchList();
		if (matchList.isEmpty()) {
			return 0;
		}
		String[] ruleNameSplit = matchList.get(0).getRule().getName().split("_");
		trackingAgendaEvent.reset();
		return Long.parseLong(ruleNameSplit[1]);
	}

	@Getter
	static final class TrackingAgendaEventListener extends DefaultAgendaEventListener {

		private final List<Match> matchList;

		TrackingAgendaEventListener() {
			matchList = new LinkedList<>();
		}

		@Override
		public void afterMatchFired(AfterMatchFiredEvent event) {
			matchList.add(event.getMatch());
		}

		public boolean isRuleFired(String ruleName) {
			return matchList.stream()
				.map(Match::getRule).map(org.kie.api.definition.rule.Rule::getName)
				.anyMatch(n -> Objects.equals(n, ruleName));
		}

		public void reset() {
			matchList.clear();
		}

		@Override
		public String toString() {
			return matchList.isEmpty()
				? "No matches occurred."
				: "Matches: " + matchList.stream()
				.map(m -> "\n  rule: " + m.getRule().getName()).collect(Collectors.joining());
		}

	}

}