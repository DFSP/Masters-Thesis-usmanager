package pt.unl.fct.microservicemanagement.mastermanager.manager.rulesystem.condition;

import org.springframework.dao.DataIntegrityViolationException;
import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
public class ConditionsService {

  private final ConditionRepository conditions;

  public ConditionsService(ConditionRepository conditions) {
    this.conditions = conditions;
  }

  public List<ConditionEntity> getConditions() {
    return conditions.findAll();
  }

  public ConditionEntity getCondition(Long id) {
    return conditions.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ConditionEntity.class, "id", id.toString()));
  }

  public ConditionEntity getCondition(String conditionName) {
    return conditions.findByNameIgnoreCase(conditionName).orElseThrow(() ->
        new EntityNotFoundException(ConditionEntity.class, "conditionName", conditionName));
  }

  public ConditionEntity addCondition(ConditionEntity condition) {
    assertConditionDoesntExist(condition);
    log.debug("Saving condition {}", ToStringBuilder.reflectionToString(condition));
    return conditions.save(condition);
  }

  public ConditionEntity updateCondition(String conditionName, ConditionEntity newCondition) {
    var condition = getCondition(conditionName);
    ObjectUtils.copyValidProperties(newCondition, condition);
    condition = conditions.save(condition);
    return condition;
  }

  public void deleteCondition(String conditionName) {
    var condition = getCondition(conditionName);
    conditions.delete(condition);
  }

  private void assertConditionDoesntExist(ConditionEntity condition) {
    var name = condition.getName();
    if (conditions.hasCondition(name)) {
      throw new DataIntegrityViolationException("Condition '" + name + "' already exists");
    }
  }

}
