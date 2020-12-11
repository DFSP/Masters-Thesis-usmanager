package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.HostDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class HostDecisionDTO {

	private Long id;
	private Decision decision;
	private HostRule rule;
	private String publicIpAddress;
	private String privateIpAddress;
	private Timestamp timestamp;
	private Set<HostDecisionValueDTO> hostDecisionValues;
	/*@JsonProperty("isNew")
	private boolean isNew; */

	public HostDecisionDTO(HostDecision hostDecision) {
		this.id = hostDecision.getId();
		this.decision = hostDecision.getDecision();
		this.rule = hostDecision.getRule();
		this.publicIpAddress = hostDecision.getPublicIpAddress();
		this.privateIpAddress = hostDecision.getPrivateIpAddress();
		this.timestamp = hostDecision.getTimestamp();
		this.hostDecisionValues = hostDecision.getHostDecisionValues().stream().map(HostDecisionValueDTO::new).collect(Collectors.toSet());
		/*this.isNew = hostDecision.isNew();*/
	}

	@JsonIgnore
	public HostDecision toEntity() {
		HostDecision hostDecision = HostDecision.builder()
			.id(id)
			.decision(decision)
			.rule(rule)
			.publicIpAddress(publicIpAddress)
			.privateIpAddress(privateIpAddress)
			.timestamp(timestamp)
			.hostDecisionValues(hostDecisionValues != null ? hostDecisionValues.stream().map(HostDecisionValueDTO::toEntity).collect(Collectors.toSet()) : new HashSet<>())
			.build();
		/*hostDecision.setNew(hostDecision.isNew());*/
		return hostDecision;
	}
}
