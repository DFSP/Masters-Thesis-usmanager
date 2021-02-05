package pt.unl.fct.miei.usmanagement.manager.rulesystem.rules;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
public class RuleConditionKey implements Serializable {

	private static final long serialVersionUID = -7826161915660858243L;

	@Column(name = "rule_id")
	private Long ruleId;

	@Column(name = "condition_id")
	private Long conditionId;

}

