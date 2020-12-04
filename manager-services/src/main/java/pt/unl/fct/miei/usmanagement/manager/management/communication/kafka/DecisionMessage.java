package pt.unl.fct.miei.usmanagement.manager.management.communication.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.monitoring.HostEvent;
import pt.unl.fct.miei.usmanagement.manager.monitoring.ServiceEvent;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;

import javax.persistence.CascadeType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class DecisionMessage {

	private Long id;
	private RuleDecisionEnum ruleDecision;
	private ComponentType componentType;
	private Set<ServiceEvent> serviceEvents;
	private Set<HostEvent> hostEvents;

	public DecisionMessage(Decision decision) {
		this.id = decision.getId();
		this.ruleDecision = decision.getRuleDecision();
		this.componentType = decision.getComponentType();
		this.serviceEvents = decision.getServiceEvents();
		this.hostEvents = decision.getHostEvents();
	}

}
