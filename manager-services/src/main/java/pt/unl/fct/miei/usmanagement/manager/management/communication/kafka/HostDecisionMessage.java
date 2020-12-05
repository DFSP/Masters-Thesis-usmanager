package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecisionValue;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.sql.Timestamp;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostDecisionMessage {

	private Long id;
	private Decision decision;
	private HostRule rule;
	private String publicIpAddress;
	private String privateIpAddress;
	private Timestamp timestamp;
	private Set<HostDecisionValue> hostDecisions;

	public HostDecisionMessage(HostDecision hostDecision) {
		this.id = hostDecision.getId();
		this.decision = hostDecision.getDecision();
		this.rule = hostDecision.getRule();
		this.publicIpAddress = hostDecision.getPublicIpAddress();
		this.privateIpAddress = hostDecision.getPrivateIpAddress();
		this.timestamp = hostDecision.getTimestamp();
		this.hostDecisions = hostDecision.getHostDecisions();
	}

	public HostDecision get() {
		return HostDecision.builder()
			.id(id)
			.decision(decision)
			.rule(rule)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.timestamp(timestamp)
			.hostDecisions(hostDecisions)
			.build();
	}
}
